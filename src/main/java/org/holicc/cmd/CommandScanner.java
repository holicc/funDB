package org.holicc.cmd;

import org.holicc.cmd.annotation.Command;

import java.util.HashMap;

public class CommandScanner {


    public HashMap<String, Command> scan() {
        String packageName = this.getClass().getPackageName();
        ClassLoader classLoader = this.getClass().getClassLoader();
        classLoader.resources(packageName.replaceAll("\\.", "/"))
                .forEach(url -> System.out.println(url.toString()));
        return null;
    }

}
