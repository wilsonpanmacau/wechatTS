package com.mcu32.firstq.wechat.util.springMVCBinder;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.propertyeditors.PropertiesEditor;

public class IntegerEditor extends PropertiesEditor {    
    @Override    
    public void setAsText(String text) throws IllegalArgumentException {
    	Integer i=null;
    	if (StringUtils.isEmpty(text)) {
    		i = null;    
        }else{
        	i=Integer.parseInt(text.replace(",", ""));
        }
        setValue(i);    
    }    
    
    @Override    
    public String getAsText() {    
        return getValue().toString();    
    }    
}
