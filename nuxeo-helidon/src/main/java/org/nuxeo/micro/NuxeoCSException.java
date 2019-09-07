/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *     Nuxeo
 *
 */
package org.nuxeo.micro.helidon;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class NuxeoCSException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private LinkedList<String> infos;

    protected int statusCode = SC_INTERNAL_SERVER_ERROR;

    public NuxeoCSException() {
    }

    public NuxeoCSException(int statusCode) {
        this.statusCode = statusCode;
    }

    public NuxeoCSException(String message) {
        super(message);
    }

    public NuxeoCSException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public NuxeoCSException(String message, Throwable cause) {
        super(message, cause);
    }

    public NuxeoCSException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public NuxeoCSException(Throwable cause) {
        super(cause);
    }

    public NuxeoCSException(Throwable cause, int statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }

    public void addInfo(String info) {
        if (infos == null) {
            infos = new LinkedList<>();
        }
        infos.addFirst(info);
    }

    public List<String> getInfos() {
        return infos == null ? Collections.emptyList() : infos;
    }

    public String getOriginalMessage() {
        return super.getMessage();
    }

    @Override
    public String getMessage() {
        String message = getOriginalMessage();
        if (infos == null) {
            return message;
        } else {
            StringBuilder sb = new StringBuilder();
            for (String info : infos) {
                sb.append(info);
                sb.append(", ");
            }
            sb.append(message);
            return sb.toString();
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

}
