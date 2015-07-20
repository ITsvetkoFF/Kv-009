package org.ecomap.android.app.data.model;

import android.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpCookie;
import java.net.URI;

/**
 * Created by yridk_000 on 15.07.2015.
 */
public class SerializableUriCookiePair implements Serializable {

    private static final long serialVersionUID = 8628587700329421486L;

    private String name;
    private String value;
    private String comment;
    private String domain;
    private long maxAge;
    private String path;
    private boolean secure;
    private int version;
    private URI uri;

    /**
     * Creates a cookie.
     */
    public SerializableUriCookiePair(final URI uri, final HttpCookie cookie) {
        this.name = cookie.getName();
        this.value = cookie.getValue();
        this.comment = cookie.getComment();
        this.domain = cookie.getDomain();
        this.maxAge = cookie.getMaxAge();
        this.path = cookie.getPath();
        this.secure = cookie.getSecure();
        this.version = cookie.getVersion();
        this.uri = uri;
    }

    /**
     * Builds a Cookie object from this object.
     */
    public Pair<URI, HttpCookie> toCookie() {
        final HttpCookie cookie = new HttpCookie(name, value);
        cookie.setComment(comment);
        //Otherwise null pointer exception
        if (domain != null) {
            cookie.setDomain(domain);
        }
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        cookie.setSecure(secure);
        cookie.setVersion(version);
        return new Pair<>(this.uri, cookie);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(uri);
        out.writeUTF(defaultIfNull(name, ""));
        out.writeUTF(defaultIfNull(value, ""));
        out.writeUTF(defaultIfNull(comment, ""));
        out.writeUTF(defaultIfNull(domain, ""));
        out.writeLong(maxAge);
        out.writeUTF(defaultIfNull(path, ""));
        out.writeBoolean(secure);
        out.writeInt(version);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.uri = (URI) in.readObject();
        name = in.readUTF();
        value = in.readUTF();
        comment = in.readUTF();
        domain = in.readUTF();
        maxAge = in.readLong();
        path = in.readUTF();
        secure = in.readBoolean();
        version = in.readInt();
    }


    private <T> T defaultIfNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

}
