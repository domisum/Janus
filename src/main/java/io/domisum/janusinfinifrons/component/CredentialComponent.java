package io.domisum.janusinfinifrons.component;

import io.domisum.janusinfinifrons.credential.Credential;

public interface CredentialComponent
{

	void injectCredential(Credential credential);

	String getCredentialId();

}
