package org.opentosca.yamlconverter.yamlmodel.tosca.nodes;

public class Compute extends Root {

	private Integer disk_size;
	private Integer num_cpus;
	private Integer mem_size;
	private String os_arch;
	private String os_type;
	private String os_distribution;
	private String os_version;

	public Compute() {
		this.disk_size = -1;
		this.num_cpus = -1;
		this.mem_size = -1;
		this.os_arch = "";
		this.os_type = "";
		this.os_distribution = "";
		this.os_version = "";
	}

	public Integer getDisk_size() {
		return this.disk_size;
	}

	public void setDisk_size(Integer disk_size) {
		if (disk_size != null) {
			this.disk_size = disk_size;
		}
	}

	public Integer getNum_cpus() {
		return this.num_cpus;
	}

	public void setNum_cpus(Integer num_cpus) {
		if (num_cpus != null) {
			this.num_cpus = num_cpus;
		}
	}

	public Integer getMem_size() {
		return this.mem_size;
	}

	public void setMem_size(Integer mem_size) {
		if (mem_size != null) {
			this.mem_size = mem_size;
		}
	}

	public String getOs_arch() {
		return this.os_arch;
	}

	public void setOs_arch(String os_arch) {
		if (os_arch != null) {
			this.os_arch = os_arch;
		}
	}

	public String getOs_type() {
		return this.os_type;
	}

	public void setOs_type(String os_type) {
		if (os_type != null) {
			this.os_type = os_type;
		}
	}

	public String getOs_distribution() {
		return this.os_distribution;
	}

	public void setOs_distribution(String os_distribution) {
		if (os_distribution != null) {
			this.os_distribution = os_distribution;
		}
	}

	public String getOs_version() {
		return this.os_version;
	}

	public void setOs_version(String os_version) {
		if (os_version != null) {
			this.os_version = os_version;
		}
	}
}