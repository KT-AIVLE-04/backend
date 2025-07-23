package kt.aivle.auth.application.service;

import kt.aivle.auth.application.port.out.UserRepositoryPort;
import kt.aivle.auth.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLoginFailService {
    private final UserRepositoryPort userRepositoryPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseFailCount(User user) {
        user.increaseLoginFailCount();
        userRepositoryPort.save(user);
    }
}
