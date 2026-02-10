package com.github.martinambrus.rdapi.utils;

import com.github.martinambrus.rdapi.game.Game;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModLoader {

    public void loadJarFile( String path ) throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        java.net.URL url = new File( path ).toURI().toURL();
        java.lang.reflect.Method method = java.net.URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{java.net.URL.class});
        method.setAccessible(true); /*promote the method to public access*/
        method.invoke(Thread.currentThread().getContextClassLoader(), new Object[]{url});
    }

    public void loadMod(String jarFilePath, Game gameInstance ) {
        try ( JarFile jf = new JarFile( jarFilePath ) ) {
            for (Enumeration<JarEntry> en = jf.entries(); en.hasMoreElements(); ) {
                JarEntry e = en.nextElement();
                String name = e.getName();
                if ( name.endsWith(".class"))  {
                    // Strip out ".class" and reformat path to package name
                    String javaName = name.substring(0, name.lastIndexOf('.')).replace('/', '.');

                    // only check the actual class name, not the full path
                    String[] javaNameSplitted = javaName.split( "\\." );
                    String javaClassName = javaNameSplitted[ javaNameSplitted.length - 1 ];

                    //System.out.print("Checking "+javaName+" (" + javaClassName + ") ... \n");
                    if ( javaClassName.startsWith( "RDMod" ) && !javaClassName.contains( "$" ) ) {
                        // load the actual JAR file
                        try {
                            this.loadJarFile( jarFilePath );
                            final Class<?> cl = Class.forName(javaName);
                            final Constructor<?> cons = cl.getConstructor( Game.class );
                            cons.newInstance( gameInstance );
                            System.out.print( javaClassName + " loaded\n");
                        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                                 IllegalAccessException | InstantiationException ex) { // E.g. internal classes, ...
                            System.out.print("Failed to load mod from " + jarFilePath + ", class " + javaClassName + " failed to instantiate.\n");
                            System.out.print( ex.getMessage() + "\n" );
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
