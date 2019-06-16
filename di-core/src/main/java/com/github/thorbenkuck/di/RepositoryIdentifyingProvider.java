package com.github.thorbenkuck.di;

class RepositoryIdentifyingProvider implements IdentifiableProvider<Repository> {

	private final WiredTypes wiredTypes;

	RepositoryIdentifyingProvider(WiredTypes wiredTypes) {
		this.wiredTypes = wiredTypes;
	}

	@Override
	public Class type() {
		return Repository.class;
	}

	@Override
	public Class[] wiredTypes() {
		return new Class[] {
				Repository.class,
				WiredTypes.class
		};
	}

	@Override
	public boolean lazy() {
		return false;
	}

	@Override
	public Repository get() {
		return wiredTypes;
	}
}
