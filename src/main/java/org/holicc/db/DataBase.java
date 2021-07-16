package org.holicc.db;

import org.holicc.server.Command;
import org.holicc.server.Response;

public interface DataBase {

    Response execute(Command cmd) throws DBException;

}
