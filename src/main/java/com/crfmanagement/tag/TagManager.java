package com.crfmanagement.tag;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton to manage available tags. Provides methods to add/remove tags and persistence to tags.dat.
 */
public class TagManager {
    private static TagManager instance;
    private final List<Tag> tags;

    private TagManager() {
        tags = new ArrayList<>();
        loadTags();
    }

    public static TagManager getInstance() {
        if (instance == null) {
            instance = new TagManager();
        }
        return instance;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        saveTags();
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        saveTags();
    }

    public void saveTags() {
        try (FileOutputStream fos = new FileOutputStream("tags.dat");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(tags);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTags() {
        try (FileInputStream fis = new FileInputStream("tags.dat");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            List<Tag> loaded = (List<Tag>) ois.readObject();
            tags.clear();
            tags.addAll(loaded);
        } catch (Exception e) {
            // No tags file or error, start with empty list
        }
    }
}
