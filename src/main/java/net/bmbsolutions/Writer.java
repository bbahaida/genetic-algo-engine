package net.bmbsolutions;

import lombok.SneakyThrows;

import java.io.FileWriter;
import java.util.List;

public abstract class Writer<O> {
    public void write(O output, String path) {
        writeLines(convertToLines(output), path);
    }

    protected abstract List<String> convertToLines(O output);

    @SneakyThrows
    public void writeLines(List<String> lines, String path) {
        try (FileWriter fileWriter = new FileWriter(path)) {
            for (String line : lines) {
                fileWriter.write(line + "\n");
            }
        }
    }
}
