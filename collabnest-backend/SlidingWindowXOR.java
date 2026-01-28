import java.util.*;

public class SlidingWindowXOR {
    static class Pair{
        long val;
        int idx;
        public Pair(long v,int i){
            this.val = v;
            this.idx = i;
        }
    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int k = sc.nextInt();
        long[] arr = new long[n];
        long x = sc.nextLong(),  a = sc.nextLong(), b = sc.nextLong(), c = sc.nextLong();
        arr[0] = x;
        for (int i = 1; i < n; i++) {
            x = (1L*a*arr[i-1]+b)%c;
            arr[i] = x;
        }
        
        Deque<Pair> q = new ArrayDeque<>();
        for(int i=0;i<k;i++){
            while(!q.isEmpty() && q.peekLast().val>arr[i]) q.pollLast();
            q.offerLast(new Pair(arr[i],i));
        }
        long xor = q.peekFirst().val;
        for(int i=1;i<n-k+1;i++)
        {
            if(q.peekFirst().idx == i-1) q.pollFirst();
            while(!q.isEmpty() && q.peekLast().val>arr[i+k-1]) q.pollLast();
            q.offerLast(new Pair(arr[i+k-1],i+k-1));
            xor ^= q.peekFirst().val;
        }
        System.out.println(xor);
    }
}

/*
8 5
3 7 1 11
Output:

3
*/
