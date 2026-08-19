package com.example.petnow.service;

import com.example.petnow.dto.response.HostPlaceListResponse;
import com.example.petnow.mapper.HostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HostServiceImpl implements HostService {

    private final HostMapper hostMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HostPlaceListResponse> getPlacesByUserId(Long userId) {
        return hostMapper.findAllByUserId(userId);
    }
}
