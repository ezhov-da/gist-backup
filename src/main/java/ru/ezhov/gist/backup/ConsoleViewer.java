package ru.ezhov.gist.backup;

public class ConsoleViewer implements Viewer {
    @Override
    public void show(String text) {
        System.out.print(text);
    }
}
