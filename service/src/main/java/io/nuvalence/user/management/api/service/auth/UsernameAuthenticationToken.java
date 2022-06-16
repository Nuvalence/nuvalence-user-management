package io.nuvalence.user.management.api.service.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Custom Username Token for Server authorization.
 */
public class UsernameAuthenticationToken extends AbstractAuthenticationToken {

    private final Collection<GrantedAuthority> authorities;
    private final String principleUid;
    private final String userEmail;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     * @param principleUid principle of token, user's uid.
     * @param userEmail user's email on token.
     */
    public UsernameAuthenticationToken(Collection<? extends GrantedAuthority> authorities,
                                       String principleUid,
                                       String userEmail) {
        super(authorities);
        this.authorities = java.util.List.copyOf(authorities);
        this.principleUid = principleUid;
        this.userEmail = userEmail;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.authorities;
    }

    @Override
    public Object getPrincipal() {
        return this.principleUid;
    }

    public String getUserEmail() {
        return userEmail;
    }
}