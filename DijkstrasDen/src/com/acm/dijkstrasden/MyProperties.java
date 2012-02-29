package com.acm.dijkstrasden;

public class MyProperties {
    private static MyProperties mInstance= null;

    public int levelType;

    protected MyProperties(){}

    public static synchronized MyProperties getInstance(){
        if(null == mInstance){
                mInstance = new MyProperties();
        }
        return mInstance;
    }
}
