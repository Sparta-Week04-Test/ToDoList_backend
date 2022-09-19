package sparta.seed.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sparta.seed.exception.CustomException;
import sparta.seed.exception.ErrorCode;
import sparta.seed.image.domain.DeletedUrlPath;
import sparta.seed.image.domain.Image;
import sparta.seed.image.repository.DeletedUrlPathRepository;
import sparta.seed.image.repository.ImageRepository;
import sparta.seed.login.domain.Member;
import sparta.seed.login.repository.MemberRepository;
import sparta.seed.message.Message;
import sparta.seed.s3.S3Dto;
import sparta.seed.s3.S3Uploader;
import sparta.seed.sercurity.UserDetailsImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageService {

    private final S3Uploader s3Uploader;
    private final DeletedUrlPathRepository deletedUrlPathRepository;
    private final MemberRepository memberRepository;
    private final ImageRepository imageRepository;
    @Transactional
    public String saveProfileImage(MultipartFile multipartFile, UserDetailsImpl userDetailsImpl) throws IOException {
        if (multipartFile.isEmpty())
            throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);

        S3Dto s3Dto = s3Uploader.upload(multipartFile);
        Member member = getMember(userDetailsImpl);
        saveDeletedUrlPath(member);

        member.setProfileImage(s3Dto.getUploadImageUrl());
        memberRepository.save(member);

        return s3Dto.getUploadImageUrl();
    }
    @Transactional
    public String deleteProfileImage(UserDetailsImpl userDetailsImpl) {
        Member member = getMember(userDetailsImpl);
//        Member member = userDetailsImpl.getMember();
        saveDeletedUrlPath(member);
        member.setProfileImage(null); //기본 이미지 만들어지면 해당 주소 넣기
        return Message.IMAGE_DELETE_SUCCESS.getMessage();
    }
    public List<String> getBoastImage(UserDetailsImpl userDetailsImpl) {
        if (!imageRepository.existsByMember(userDetailsImpl.getMember()))
            throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);

        return imageRepository.getBoastImage(userDetailsImpl.getId());
    }

    public String saveBoastImage(MultipartFile multipartFile, UserDetailsImpl userDetailsImpl) throws IOException {
        if (multipartFile.isEmpty())
            throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
        Member member = getMember(userDetailsImpl);

        S3Dto s3Dto = s3Uploader.upload(multipartFile);
        Image image = Image.builder()
                .imgUrl(s3Dto.getUploadImageUrl())
                .urlPath(s3Dto.getFileName())
                .member(member)
                .build();

        imageRepository.save(image);
        return Message.IMAGE_UPLOAD_SUCCESS.getMessage();
    }
    @Transactional
    public String deleteBoastImage(Long boastId) {
        if(!imageRepository.existsById(boastId))
            throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
        imageRepository.deleteById(boastId);
        return Message.IMAGE_DELETE_SUCCESS.getMessage();
    }
    public Member getMember(UserDetailsImpl userDetailsImpl) {
        return memberRepository.findById(userDetailsImpl.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public void saveDeletedUrlPath(Member member) {
        DeletedUrlPath deletedUrlPath = new DeletedUrlPath();
        deletedUrlPath.setDeletedUrlPath(member.getProfileImage());
        deletedUrlPathRepository.save(deletedUrlPath);
    }

    public void removeS3Image() {
        List<DeletedUrlPath> deletedUrlPathList = deletedUrlPathRepository.findAll();
        for (DeletedUrlPath deletedUrlPath : deletedUrlPathList) {
            s3Uploader.remove(deletedUrlPath.getDeletedUrlPath());
        }
        deletedUrlPathRepository.deleteAll();
    }



}
