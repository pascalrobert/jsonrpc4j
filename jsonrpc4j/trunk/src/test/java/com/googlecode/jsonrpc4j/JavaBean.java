package com.googlecode.jsonrpc4j;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class JavaBean {
    private String name;
    private Long id;
    private int age;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JavaBean)) {
			return false;
		}
		JavaBean bean = (JavaBean)obj;
		return new EqualsBuilder()
			.append(this.name, bean.name)
			.append(this.id, bean.id)
			.append(this.age, bean.age)
			.isEquals();
		
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(this.name)
			.append(this.id)
			.append(this.age)
			.toHashCode();
	}
    
}