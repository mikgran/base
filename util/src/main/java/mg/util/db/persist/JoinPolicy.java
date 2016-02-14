package mg.util.db.persist;

public enum JoinPolicy {
    JOIN("JOIN "), LEFT_JOIN("LEFT JOIN ");

    private final String policy;

    private JoinPolicy(String policy) {
        this.policy = policy;
    }

    public boolean equalsPolicy(String other) {
        return (other == null) ? false : policy.equals(other);
    }

    public String toString() {
        return this.policy;
    }
}