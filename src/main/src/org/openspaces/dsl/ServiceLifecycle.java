package org.openspaces.dsl;

public class ServiceLifecycle  {

	private Object init;
	
	private Object preInstall;
	private Object install;
	private Object postInstall;
	
	private Object preStart;
	private Object start;
	private Object postStart;
	
	private Object preStop;
	private Object stop;
	private Object postStop;
	
	private Object shutdown;

	public ServiceLifecycle() {
		
	}
	public Object getInit() {
		return init;
	}

	public void setInit(Object init) {
		this.init = init;
	}

	public Object getPreInstall() {
		return preInstall;
	}

	public void setPreInstall(Object preInstall) {
		this.preInstall = preInstall;
	}

	public Object getInstall() {
		return install;
	}

	public void setInstall(Object install) {
		this.install = install;
	}

	public Object getPostInstall() {
		return postInstall;
	}

	public void setPostInstall(Object postInstall) {
		this.postInstall = postInstall;
	}

	public Object getPreStart() {
		return preStart;
	}

	public void setPreStart(Object preStart) {
		this.preStart = preStart;
	}

	public Object getStart() {
		return start;
	}

	public void setStart(Object start) {
		this.start = start;
	}

	public Object getPostStart() {
		return postStart;
	}

	public void setPostStart(Object postStart) {
		this.postStart = postStart;
	}

	public Object getPreStop() {
		return preStop;
	}

	public void setPreStop(Object preStop) {
		this.preStop = preStop;
	}

	public Object getStop() {
		return stop;
	}

	public void setStop(Object stop) {
		this.stop = stop;
	}

	public Object getPostStop() {
		return postStop;
	}

	public void setPostStop(Object postStop) {
		this.postStop = postStop;
	}

	public Object getShutdown() {
		return shutdown;
	}

	public void setShutdown(Object shutdown) {
		this.shutdown = shutdown;
	}
	
	
	
}
