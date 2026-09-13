package uz.app.projectv1.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.app.projectv1.common.exception.NotFoundException;
import uz.app.projectv1.user.dto.UserResponse;
import uz.app.projectv1.user.entity.UserEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi", id));
    }

    public List<UserResponse> getAll() {
        return userMapper.toResponseList(userRepository.findAll());
    }

    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi", email));
    }

    @Transactional
    public void delete(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi", id));
        userRepository.delete(user);
    }
}
