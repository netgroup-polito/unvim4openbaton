package org.polito.model.nffg;

import java.util.ArrayList;
import java.util.List;

public class FlowRule {
	private String id;
	private int priority;
	private Match match;
	private List<Action> actions = new ArrayList<>();

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public Match getMatch()
	{
		return match;
	}

	public void setMatch(Match match)
	{
		this.match = match;
	}

	public List<Action> getActions()
	{
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public void addAction(Action action) {
		actions.add(action);
	}

}
