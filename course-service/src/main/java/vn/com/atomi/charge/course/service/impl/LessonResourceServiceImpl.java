package vn.com.atomi.charge.course.service.impl;

import org.springframework.stereotype.Service;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.course.mapper.LessonResourceMapper;
import vn.com.atomi.charge.course.model.dto.LessonResourceDto;
import vn.com.atomi.charge.course.model.entity.LessonResourceEntity;
import vn.com.atomi.charge.course.repository.LessonResourceRepository;
import vn.com.atomi.charge.course.service.interfaces.LessonResourceService;

@Service
public class LessonResourceServiceImpl
    extends BaseService<
        LessonResourceRepository,
        LessonResourceDto,
        LessonResourceEntity,
        LessonResourceMapper>
    implements LessonResourceService {
}
