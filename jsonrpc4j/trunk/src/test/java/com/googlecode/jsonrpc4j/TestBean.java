package com.googlecode.jsonrpc4j;

public class TestBean {
    private String name;
    private TestBean child;
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the child
     */
    public TestBean getChild() {
        return child;
    }
    /**
     * @param child the child to set
     */
    public void setChild(TestBean child) {
        this.child = child;
    }
}
