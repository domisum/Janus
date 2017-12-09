package de.domisum.janusinfinifrons.component;

import de.domisum.janusinfinifrons.credential.Credential;

public interface CredentialComponent
{

	void injectCredential(Credential credential);

	String getCredentialId();

}
