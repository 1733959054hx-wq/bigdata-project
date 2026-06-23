package com.gzu.bigdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gzu.bigdata.entity.FileMetadata;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update; // ✨ 引入 Update


import java.util.List;

@Mapper
public interface FileMetadataMapper extends BaseMapper<FileMetadata> {

    /**
     * 物理删除记录（绕过逻辑删除）
     */
    @Delete("DELETE FROM file_metadata WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);


    /**
     * 查询回收站中的所有文件
     */
    @Select("SELECT * FROM file_metadata WHERE is_deleted = 1")
    List<FileMetadata> selectTrashFiles();

    /**
     * 查询回收站路径下的所有文件
     */
    @Select("SELECT * FROM file_metadata WHERE file_path LIKE '%/Trash/%'")
    List<FileMetadata> selectFilesInTrashPath();

    // ✨✨✨ 新增：根据ID查询文件（无视逻辑删除状态） ✨✨✨
    // 这是一个原生 SQL，MyBatis-Plus 不会给它乱加条件，所以能查到回收站里的文件
    @Select("SELECT * FROM file_metadata WHERE id = #{id}")
    FileMetadata selectByIdPhysical(@Param("id") Long id);

    /**
     * ✨✨✨ 新增：批量更新子文件路径 (用于文件夹重命名) ✨✨✨
     * 将所有以 oldPath 开头的路径替换为 newPath 前缀
     */
    @Update("UPDATE file_metadata SET file_path = CONCAT(#{newPath}, SUBSTRING(file_path, LENGTH(#{oldPath}) + 1)) " +
            "WHERE file_path LIKE CONCAT(#{oldPath}, '/%') AND is_deleted = 0")
    void updateChildPaths(@Param("oldPath") String oldPath, @Param("newPath") String newPath);



    // ✨✨✨ 新增：强制还原文件（绕过逻辑删除保护） ✨✨✨
    @Update("UPDATE file_metadata SET is_deleted = 0, delete_time = NULL, file_path = #{filePath}, parent_id = #{parentId}, update_time = NOW() WHERE id = #{id}")
    int restoreFile(@Param("id") Long id, @Param("filePath") String filePath, @Param("parentId") Long parentId);
}
