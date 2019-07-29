package com.kirchnersolutions.database.sessions;

public class SessionFactory {

    /**
     * Creates new session of given type.
     * Returns null if type is invalid.
     * @param type
     * @return
     */
    public static Session sessionFactory(String type){
        return SessionFactory(type);
    }

    private static Session SessionFactory(String type){
        if(type.equals(new String("web"))){
            return new WebSession();
        }
        if(type.equals(new String("socket"))){
            return new SocketSession();
        }
        return null;
    }

}