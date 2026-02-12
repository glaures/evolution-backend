package expondo.evolution.config;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class AuditRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        AuditRevisionEntity rev = (AuditRevisionEntity) revisionEntity;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            rev.setUserEmail(jwt.getClaimAsString("upn"));
            rev.setUserName(jwt.getClaimAsString("name"));
        } else {
            rev.setUserEmail("system");
            rev.setUserName("System");
        }
    }
}