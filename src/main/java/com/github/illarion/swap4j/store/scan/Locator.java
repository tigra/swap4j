package com.github.illarion.swap4j.store.scan;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

public class Locator implements Comparable<Locator>{
    private String path;
    private UUID id;

    public Locator(UUID id, String path) {
        this.path = path;
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Locator() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(shortRepresentation(id)).append(path);
        return sb.toString();
    }

    private String shortRepresentation(UUID id) {
        if (null == id) {
            return null;
        } else {
            return Long.toHexString(id.getMostSignificantBits()) + "-" + Long.toHexString(id.getLeastSignificantBits());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Locator locator = (Locator) o;

        if (id != null ? !id.equals(locator.id) : locator.id != null) return false;
        if (path != null ? !path.equals(locator.path) : locator.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Locator that) {
        if (null == this.id) {
            return null == that.id ? 0 : -1;
        }
        if (null == that.id) {
            return 0;
        }
        int uuidResult = this.id.compareTo(that.id);
        if (uuidResult != 0) {
            return uuidResult;
        }
        if (null == this.path) {
            return null == that.path ? 0 : -1;
        }
        if (null == that.path) {
            return 1;
        }
        return this.path.compareTo(that.path);
    }

    public boolean isRoot(SerializedField serializedField) {
        return ".".equals(getPath());
    }

    public List<String> getParsedPath() {
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        List<String> parsedPath = new ArrayList<String>();

        if (!tokenizer.hasMoreTokens()) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        tokenizer.nextToken(); // "."

        while (tokenizer.hasMoreTokens()) {
            parsedPath.add(tokenizer.nextToken());
        }

        return parsedPath;
    }
}