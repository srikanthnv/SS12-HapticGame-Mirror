package com.acm.dijkstrasden;

/**
 * Global system properties, if any.
 */
public class MyProperties {
    private static MyProperties mInstance= null;

    /**
     * 0 = tutorial, 1 = game. 
     */
    public int levelType;

    protected MyProperties(){}

    public static synchronized MyProperties getInstance(){
        if(null == mInstance){
                mInstance = new MyProperties();
        }
        return mInstance;
    }
}
