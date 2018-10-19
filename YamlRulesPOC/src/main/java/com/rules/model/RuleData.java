package com.rules.model;

import java.io.Serializable;
import java.util.List;

public class RuleData implements Serializable{
	public String name;
	public String description;
	public String condition;
	public List actions;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public List getActions() {
		return actions;
	}
	public void setActions(List actions) {
		this.actions = actions;
	}
}