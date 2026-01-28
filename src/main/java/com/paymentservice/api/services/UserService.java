package com.paymentservice.api.services;

import com.paymentservice.api.dtos.UserDTO;
import com.paymentservice.api.dtos.UserResponseDTO;
import com.paymentservice.api.dtos.UserUpdateDTO;
import com.paymentservice.api.exception.UserNotFoundException;
import com.paymentservice.api.model.User;
import com.paymentservice.api.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public List<User> getAllAccounts(){
        return userRepository.findAll();
    }

    public UserResponseDTO addNewUser(UserDTO dataUser){

        verifyUniqueEmail(dataUser.email());
        verifyUniqueDocument(dataUser.document());

        User newUser = createUser(dataUser);

        userRepository.save(newUser);
        return new UserResponseDTO(newUser);
    }

    public void updateUser(Long id, UserUpdateDTO newData) {
        User userData = userRepository.findById( id )
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        updateIfPresent(newData.firstName(), userData::setFirstName);
        updateIfPresent(newData.lastName(), userData::setLastName);
        updateIfPresent(newData.password(), userData::setPassword);

        if( newData.email() != null && !newData.email().equals( userData.getEmail() ) ){
            verifyUniqueEmail(newData.email());
            userData.setEmail( newData.email() );
        }

        userRepository.save(userData);
    }

    public void deleteUser(Long id){
        userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        userRepository.deleteById(id);
    }


    private void verifyUniqueDocument(String document){
        if(userRepository.existsByDocument( document )){
            throw new DataIntegrityViolationException("Documento já está cadastrado.");
        }
    }

    private void verifyUniqueEmail(String email){
        if(userRepository.existsByEmail( email )){
            throw new DataIntegrityViolationException("Email já está cadastrado.");
        }
    }

    private User createUser(UserDTO dataUser){
        User newUser = new User();
        newUser.setFirstName(dataUser.firstName() );
        newUser.setLastName( dataUser.lastName() );
        newUser.setDocument( dataUser.document() );
        newUser.setEmail( dataUser.email() );
        newUser.setPassword( dataUser.password() );
        newUser.setUserType( dataUser.userType() );

        if(dataUser.balance() != null){
            newUser.setBalance( dataUser.balance() );
        } else{
            newUser.setBalance( new BigDecimal("0") );
        }

        return newUser;
    }

    private void updateIfPresent(String newValue, Consumer<String> data){
        if(newValue != null && !newValue.isBlank()){
            data.accept(newValue);
        }
    }
}
