package com.wornux.security.auditable;

import com.wornux.data.entity.Revision;
import com.wornux.data.entity.User;
import com.wornux.security.UserUtils;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class RevisionListenerImpl implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        final SecurityContext context = SecurityContextHolder.getContext();
        Objects.requireNonNull(context, "Context cannot be null");

        User actualUser = UserUtils.getUser();

        if (actualUser != null) {
            final String userName = actualUser.getUsername();

            // Get the IP address from the request
            Revision revision = (Revision) revisionEntity;
            revision.setModifierUser(userName);
            revision.setIpAddress("0.0.0.0");
        } else {
            Revision revision = (Revision) revisionEntity;
            revision.setModifierUser("ANONYMOUS");
            revision.setIpAddress("0.0.0.0");
        }
    }
}
