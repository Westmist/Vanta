package org.markeb;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ComTest {

   static class Member {
        String name;
        int position;    // 2=会长, 1=管理员, 0=普通成员
        int score;
        long joinTime;

        public  Member(String name, int position, int score, long joinTime) {
            this.name = name;
            this.position = position;
            this.score = score;
            this.joinTime = joinTime;
        }

        @Override
        public String toString() {
            return name + " pos=" + position + " score=" + score + " join=" + joinTime;
        }

       public String getName() {
           return name;
       }

       public int getPosition() {
           return position;
       }

       public int getScore() {
           return score;
       }

       public long getJoinTime() {
           return joinTime;
       }
   }

    public static void main(String[] args) {
        List<Member> members = Arrays.asList(
            new Member("A", 0, 120, 1000),
            new Member("B", 1, 80, 2000),
            new Member("C", 1, 90, 1500),
            new Member("D", 0, 120, 500)
        );

        System.out.println("原始顺序：");
        members.forEach(System.out::println);

        // 多条件排序：position 降序 → score 降序 → joinTime 升序（加入早优先）
        members.sort(
            Comparator.comparingInt(Member::getPosition)     // position 越大越前
                .thenComparingInt(Member::getScore)   // score 越大越前
//                .thenComparingLong(Member::getJoinTime)      // joinTime 越小越前
        );

        System.out.println("\n排序后：");
        members.forEach(System.out::println);

        // 取最优成员
        Member best = members.get(0);
        System.out.println("\n最优成员: " + best);
    }

}
