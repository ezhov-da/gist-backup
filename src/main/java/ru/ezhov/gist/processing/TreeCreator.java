package ru.ezhov.gist.processing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.*;

public class TreeCreator {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
        {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable ex) {
                //
            }
            JFrame frame = new JFrame("Деревья знаний");

            JTextField pathToFile = new JTextField("D:\\programmer\\git\\gist-backup\\src\\main\\resources\\names.txt");
            JButton button = new JButton("Загрузить");
            JPanel north = new JPanel(new BorderLayout());
            north.add(pathToFile, BorderLayout.CENTER);
            north.add(button, BorderLayout.EAST);


            JTree tree = new JTree(new Object[0]);
            tree.setEditable(true);
            tree.setCellEditor(getEditor(tree));
            JPopupMenu menu = new JPopupMenu();
            JMenuItem itemEdit = new JMenuItem("Редактировать");
            JMenuItem itemMove = new JMenuItem("Переместить выше");
            itemEdit.addActionListener(getEditActionListener(tree));
            itemMove.addActionListener(a -> {
                SwingUtilities.invokeLater(() -> {
                    TreePath treePath = tree.getSelectionPath();
                    if (treePath != null) {
                        DefaultMutableTreeNode currentNode =
                                ((DefaultMutableTreeNode) treePath.getLastPathComponent());
                        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) currentNode.getParent();
                        if (!"root".equals(getFromUserObject(parent).getEdit())) {
                            String nameParent = getFromUserObject(parent).getEdit();
                            String nameCurrentNode = getFromUserObject(currentNode).getEdit();
                            NodeData nodeDataParent = getFromUserObject(parent);
                            nodeDataParent.setEdit(nameParent + " " + nameCurrentNode);
                            parent.setUserObject(nodeDataParent);
                            int childCount = currentNode.getChildCount();
                            List<DefaultMutableTreeNode> child = new ArrayList<>();
                            for (int i = 0; i < childCount; i++) {
                                child.add((DefaultMutableTreeNode) currentNode.getChildAt(i));
                            }
                            currentNode.removeAllChildren();
                            parent.remove(currentNode);
                            child.forEach(parent::add);
                            DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
                            dtm.reload(parent);
                        }
                    }
                });
            });
            //Объединить текущий со всеми детьми
            tree.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_J && e.isControlDown()) {
                        SwingUtilities.invokeLater(() -> {
                            TreePath treePath = tree.getSelectionPath();
                            if (treePath != null) {
                                DefaultMutableTreeNode currentNode =
                                        ((DefaultMutableTreeNode) treePath.getLastPathComponent());
                                if (!"root".equals(getFromUserObject(currentNode).getEdit())) {
                                    String fullName = fullNameAfterEdit(currentNode, "").trim();
                                    String firstCharUpperCase = fullName.substring(0, 1).toUpperCase();
                                    String otherWord = fullName.substring(1);
                                    String name = firstCharUpperCase + otherWord;
                                    NodeData nodeDataCurrent = getFromUserObject(currentNode);
                                    nodeDataCurrent.setEdit(name);
                                    currentNode.setUserObject(nodeDataCurrent);
                                    currentNode.removeAllChildren();
                                    DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
                                    dtm.reload(currentNode);
                                }
                            }
                        });
                    }
                }
            });

            menu.add(itemEdit);
            menu.add(itemMove);
            tree.setComponentPopupMenu(menu);
            tree.addMouseListener(getMouseListener(tree));

            tree.setShowsRootHandles(false);
            button.addActionListener(e -> {
                loadTree(tree, pathToFile.getText());
            });

            JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
            toolBar.add(new AbstractAction() {

                {
                    putValue(Action.SHORT_DESCRIPTION, "Объеденить выбранные строки c первой");
                    putValue(Action.NAME, "ОВССП");
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    TreePath[] treePaths = tree.getSelectionPaths();
                    if (treePaths != null && treePaths.length > 0) {
                        TreePath parent = treePaths[0];
                        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent.getLastPathComponent();
                        if (!"root".equals(getFromUserObject(parentNode).getEdit())) {
                            String nameParent = getFromUserObject(parentNode).getEdit();
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(nameParent).append(" ");
                            for (int i = 1; i < treePaths.length; i++) {
                                TreePath current = treePaths[i];
                                DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) current.getLastPathComponent();
                                stringBuilder.append(getFromUserObject(currentNode).getEdit());
                                if (i + 1 != treePaths.length) {
                                    stringBuilder.append(" ");
                                }
                                parentNode.removeAllChildren();
                            }
                            NodeData nodeDataParent = getFromUserObject(parentNode);
                            nodeDataParent.setEdit(stringBuilder.toString());
                            parentNode.setUserObject(nodeDataParent);
                            DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
                            dtm.reload(parentNode);
                        }

                    }
                }
            });

            frame.add(toolBar, BorderLayout.WEST);

            JButton buttonSave = new JButton("Сохранить данные");
            buttonSave.addActionListener(a -> {
                DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
                if (dtm != null && dtm.getRoot() != null) {
                    DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
                    int childCount = root.getChildCount();
                    File file = new File("out-" + System.currentTimeMillis() + ".txt");
                    try (FileWriter fileWriter = new FileWriter(file)) {
                        for (int i = 0; i < childCount; i++) {
                            DefaultMutableTreeNode rootCurrent = (DefaultMutableTreeNode) root.getChildAt(i);
                            NodeData nodeData = getFromUserObject(rootCurrent);
                            String fullName = fullName(rootCurrent, "");
                            fileWriter.append(nodeData.getOriginal()).append("||").append(fullName).append("\n");
                        }
                        System.out.println("файл сохранен: " + file.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            tree.setExpandsSelectedPaths(true);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(north, BorderLayout.NORTH);
            panel.add(new JScrollPane(tree), BorderLayout.CENTER);

            JPanel south = new JPanel(new BorderLayout());
            south.add(buttonSave, BorderLayout.CENTER);
            panel.add(south, BorderLayout.SOUTH);
            frame.add(panel);
            frame.setSize(1000, 600);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    private static NodeData getFromUserObject(DefaultMutableTreeNode defaultMutableTreeNode) {
        return (NodeData) defaultMutableTreeNode.getUserObject();
    }

    private static String fullName(DefaultMutableTreeNode node, String name) {
        int childCount = node.getChildCount();
        name = name + "-" + getFromUserObject(node).getEdit();
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) node.getChildAt(i);
            name = fullName(n, name);
        }
        return name;
    }

    private static String fullNameAfterEdit(DefaultMutableTreeNode node, String name) {
        int childCount = node.getChildCount();
        name = name + " " + getFromUserObject(node).getEdit();
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) node.getChildAt(i);
            name = fullNameAfterEdit(n, name);
        }
        return name;
    }

    private static void loadTree(JTree tree, String pathToFile) {
        SwingUtilities.invokeLater(() -> {
            TreeModel treeModel = null;
            try {
                treeModel = createTreeModel(pathToFile);
                tree.setModel(treeModel);
                expandAllNodes(tree, 0, tree.getRowCount());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(tree, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static TreeModel createTreeModel(String pathToFile) throws Exception {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new NodeData("root", "root"));
        DefaultTreeModel dtm = new DefaultTreeModel(root);
        List<String> rows = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(pathToFile))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                rows.add(line);
            }
        }
        Collections.sort(rows);
        rows.forEach(line -> {
            String[] arrayOriginalAndForEdit = line.split("\\|\\|");
            String original = arrayOriginalAndForEdit[0];
            String edit = arrayOriginalAndForEdit[1];
            String[] array = edit.split("-");
            Stack<DefaultMutableTreeNode> stack = new Stack<>();
            for (String s : array) {
                if (s != null && !"".equals(s)) {
                    DefaultMutableTreeNode part = new DefaultMutableTreeNode(new NodeData(original, s));
                    if (stack.isEmpty()) {
                        root.add(part);
                    } else {
                        DefaultMutableTreeNode parent = stack.peek();
                        parent.add(part);
                    }
                    stack.push(part);
                }
            }
        });
        return dtm;
    }

    //https://stackoverflow.com/questions/15210979/how-do-i-auto-expand-a-jtree-when-setting-a-new-treemodel
    private static void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }

        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }


    private static TreeCellEditor getEditor(JTree t) {
        return new DefaultTreeCellEditor(t, (DefaultTreeCellRenderer) t.getCellRenderer()) {
            @Override
            public Component getTreeCellEditorComponent(JTree tree,
                                                        Object value, boolean isSelected, boolean expanded,
                                                        boolean leaf, int row) {
                return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
            }
        };
    }

    private static MouseListener getMouseListener(JTree t) {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        int rowForLocation = t.getRowForLocation(e.getPoint().x, e.getPoint().y);
                        if (rowForLocation != -1) {
                            t.setSelectionRow(rowForLocation);
                        }
                    }
                });
            }
        };
    }

    private static ActionListener getEditActionListener(JTree t) {
        return e -> {
            SwingUtilities.invokeLater(() -> {
                TreePath selectionPath = t.getSelectionPath();
                if (selectionPath != null) {
                    t.startEditingAtPath(selectionPath);
                }
            });
        };
    }

    private static class NodeData {
        private String original;
        private String edit;

        public NodeData(String original, String edit) {
            this.original = original;
            this.edit = edit;
        }

        public String getOriginal() {
            return original;
        }

        public void setOriginal(String original) {
            this.original = original;
        }

        public String getEdit() {
            return edit;
        }

        public void setEdit(String edit) {
            this.edit = edit;
        }

        @Override
        public String toString() {
            return edit;
        }
    }
}
