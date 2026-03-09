package study.blog.member.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.blog.member.entity.Member;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MemberReader {
    private final MemberRepository memberRepository;

    /**
     * 게시글 목록에 포함된 작성자의 닉네임을 조회한다.
     *
     * 작성자 ID → 닉네임 형태의 Map으로 반환한다.
     * 이를 통해 게시글마다 Member를 조회하는 N+1 문제를 방지한다.
     *
     * @param authorIds 게시글 직상지 ID 목록
     * @return 작성자 ID → 닉네임 Map
     */
    public Map<Long, String> getNicknames(List<Long> authorIds) {
        if(authorIds.isEmpty()){
            return Map.of();
        }

        return memberRepository.findAllById(authorIds)
                .stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));
    }

    /**
     * 닉네임을 조회한다.
     * @param memberId 사용자 ID
     * @return 사용자 닉네임
     */
    public String getNickName(Long memberId) {
        return memberRepository.findById(memberId)
                .map(Member::getNickname)
                .orElse("익명");
    }

}
