package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto create(UserDto userDto) {
        if (userRepository.existsByEmailIgnoreCase(userDto.getEmail())) {
            throw new ValidationException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }
        User user = userMapper.toModel(userDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        if (userDto.getEmail() != null) {
            userRepository.findByEmailIgnoreCase(userDto.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    throw new ValidationException("Пользователь с email " + userDto.getEmail() + " уже существует");
                }
            });
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto findById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id + " + userId + " не найден"));
        userRepository.deleteById(userId);
    }
}
