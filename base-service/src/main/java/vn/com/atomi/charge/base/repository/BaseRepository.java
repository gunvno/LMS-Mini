package vn.com.atomi.charge.base.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import vn.com.atomi.charge.base.model.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity, I> extends JpaRepository<E, I>, JpaSpecificationExecutor<E> {

    @Query("FROM #{#entityName} E where E.id = :id AND E.deletedAt IS NULL")
    Optional<E> findEntityById(@Param("id") I id);

    @Query("FROM #{#entityName} E where E.id = :id AND E.createdBy = :username AND E.deletedAt IS NULL")
    Optional<E> findEntityById(@Param("id") I id, @Param("username") String username);

    @Modifying
    @Query("UPDATE #{#entityName} E set E.deletedAt = :deletedAt, E.lastModifiedBy = :username, E.lastModifiedDate = :now where E.id = :id")
    int softDelete(@Param("id") I id, @Param("deletedAt") LocalDateTime deletedAt, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE #{#entityName} E set E.deletedAt = :deletedAt, E.lastModifiedBy = :username, E.lastModifiedDate = :now where E.id in :ids")
    int softDelete(@Param("ids") Iterable<I> ids, @Param("deletedAt") LocalDateTime deletedAt, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE #{#entityName} E set E.deletedAt = :deletedAt, E.lastModifiedBy = :username, E.lastModifiedDate = :now where E.createdBy = :username AND E.id = :id")
    int softDeleteCreateBy(@Param("id") I id, @Param("deletedAt") LocalDateTime deletedAt,
                           @Param("username") String username, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE #{#entityName} E set E.deletedAt = :deletedAt, E.lastModifiedBy = :username, E.lastModifiedDate = :now where E.createdBy = :username AND E.id in :ids")
    int softDeleteCreateBy(@Param("ids") Iterable<I> ids, @Param("deletedAt") LocalDateTime deletedAt,
                           @Param("username") String username, @Param("now") LocalDateTime now);

    @Query("FROM #{#entityName} E WHERE E.deletedAt IS NULL ORDER BY E.createdDate DESC ")
    List<E> getAll();

    @Query("FROM #{#entityName} E WHERE E.createdBy = :username AND E.deletedAt IS NULL ORDER BY E.createdDate DESC ")
    List<E> getAllCreatedBy(@Param("username") String username);

    @Query("FROM #{#entityName} E WHERE E.deletedAt IS NULL ORDER BY E.createdDate DESC ")
    Page<E> getAll(Pageable pageable);

    @Query("FROM #{#entityName} E WHERE E.createdBy = :username AND E.deletedAt IS NULL ORDER BY E.createdDate DESC ")
    Page<E> getAllCreatedBy(Pageable pageable, @Param("username") String username);

    @Query("FROM #{#entityName} E WHERE E.deletedAt IS NOT NULL")
    List<E> getAllDeleted();

    @Query("FROM #{#entityName} E WHERE E.lastModifiedBy = :username AND E.deletedAt IS NOT NULL")
    List<E> getAllDeletedBy(@Param("username") String username);

    @Query("FROM #{#entityName} E WHERE E.id in :ids AND E.deletedAt IS NULL ")
    List<E> getAllByIdIn(@Param("ids") Iterable<I> ids);

    @Query("FROM #{#entityName} E WHERE E.createdBy = :username AND E.id in :ids AND E.deletedAt IS NULL ")
    List<E> getAllByIdInBy(@Param("ids") Iterable<I> ids, @Param("username") String username);
}
