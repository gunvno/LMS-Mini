package vn.com.atomi.charge.notice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.notice.model.entity.NoticeRecipientEntity;
import vn.com.atomi.charge.notice.model.enums.NoticeReadStatus;

import java.util.List;
import java.util.Optional;

public interface NoticeRecipientRepository extends BaseRepository<NoticeRecipientEntity, String> {

    Page<NoticeRecipientEntity> findByUserIdAndDeletedAtIsNullOrderByCreatedDateDesc(String userId, Pageable pageable);

    long countByUserIdAndReadStatusAndDeletedAtIsNull(String userId, NoticeReadStatus readStatus);

    Optional<NoticeRecipientEntity> findByIdAndUserIdAndDeletedAtIsNull(String id, String userId);

    List<NoticeRecipientEntity> findByUserIdAndReadStatusAndDeletedAtIsNull(String userId, NoticeReadStatus readStatus);
}
