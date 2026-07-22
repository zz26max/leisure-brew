package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.constant.MessageConstant;
import com.sky.entity.AddressBook;
import com.sky.exception.AddressBookBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressBookServiceImpl implements AddressBookService {
    private final AddressBookMapper addressBookMapper;

    /**
     * 条件查询
     *
     * @param addressBook
     * @return
     */
    public List<AddressBook> list(AddressBook addressBook) {
        addressBook.setUserId(currentUserId());
        return addressBookMapper.list(addressBook);
    }

    /**
     * 新增地址
     *
     * @param addressBook
     */
    public void save(AddressBook addressBook) {
        addressBook.setId(null);
        addressBook.setUserId(currentUserId());
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    public AddressBook getById(Long id) {
        return requireOwnedAddress(id, currentUserId());
    }

    /**
     * 根据id修改地址
     *
     * @param addressBook
     */
    public void update(AddressBook addressBook) {
        Long userId = currentUserId();
        requireOwnedAddress(addressBook.getId(), userId);
        addressBook.setUserId(userId);
        if (addressBookMapper.update(addressBook) == 0) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     */
    @Transactional
    public void setDefault(AddressBook addressBook) {
        Long userId = currentUserId();
        requireOwnedAddress(addressBook.getId(), userId);

        addressBook.setIsDefault(0);
        addressBook.setUserId(userId);
        addressBookMapper.updateIsDefaultByUserId(addressBook);

        addressBook.setIsDefault(1);
        if (addressBookMapper.update(addressBook) == 0) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }
    }

    /**
     * 根据id删除地址
     *
     * @param id
     */
    public void deleteById(Long id) {
        if (addressBookMapper.deleteByIdAndUserId(id, currentUserId()) == 0) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }
    }

    private AddressBook requireOwnedAddress(Long id, Long userId) {
        if (id == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }
        AddressBook addressBook = addressBookMapper.getByIdAndUserId(id, userId);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }
        return addressBook;
    }

    private Long currentUserId() {
        return BaseContext.getCurrentId();
    }
}
