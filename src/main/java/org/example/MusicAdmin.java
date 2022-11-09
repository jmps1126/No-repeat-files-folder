package org.example;

import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@NoArgsConstructor
public class MusicAdmin {

  public static final String OMITTED_FILE = ".DS_Store";

  void managementMusic (String from, String to, String extension) {
    Path initialPath = Paths.get(from);
    Path finalPath = Paths.get(to);

    AtomicReference<Integer> repeatedCont = new AtomicReference<>(0);
    AtomicReference<Long> sizeFileDeleted = new AtomicReference<>(0L);

    try (Stream<Path> music = Files.list(Paths.get(initialPath.toUri()))) {

      //create directory for no-repeat music
      Files.createDirectories(finalPath);

      // filter duplicate tracks
      List<Path> noRepeatMusic = music.filter(trackStore ->
          !trackStore.getFileName().toString().equalsIgnoreCase(OMITTED_FILE))
        .map(track -> {

        String fileName = track.getFileName().toString();
        Path newPath = initialPath.resolve(Path.of(removeExtension(fileName.substring(removeCounter(fileName)))
          .trim()
          .concat(extension)));
        try {
          Files.move(track, newPath);
        } catch (IOException e) {
          try {
            sizeFileDeleted.getAndSet(sizeFileDeleted.get() + Files.size(newPath));
            repeatedCont.getAndSet(repeatedCont.get() + 1);
            Files.delete(track);
          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }
        }
        return newPath;
      }).distinct()
        .collect(Collectors.toList());

      // move music destination folder
      noRepeatMusic.forEach(trackId -> {
        try {
          Files.move(trackId, finalPath.resolve(trackId.getFileName()));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

      System.out.println("process executed successfully");
      System.out.println("there was deleted: " + repeatedCont.get().toString() + " tracks");
      System.out.println("Megabytes " + sizeFileDeleted.get() / 1024 / 1024 + " storage optimized");

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String removeExtension(String fileName) {
    StringBuilder stringBuffer = new StringBuilder(fileName);
    stringBuffer.delete(fileName.length() - 4, fileName.length());
    return stringBuffer.toString();
  }

  public static int removeCounter(String fileName) {
    int counterSize = 1;
    Pattern pattern =  Pattern.compile("^[0-9]*$");

    for (int i = 0; i < fileName.length(); i++) {

      Matcher matcher = pattern.matcher(String.valueOf(fileName.charAt(i)));

      if (matcher.matches()){
        counterSize++;
      } else {
        break;
      }
    }

    return counterSize;
  }

}
