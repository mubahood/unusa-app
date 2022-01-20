package app.unusa.app.model;

public class UnusaTransacton {
    public int transactionAmount;
    public String transactionId;
    public int dueDate,
            dueMonth,
            dueYear;
    public UnusaUser
            personResponsible,
            recordedBy;
    public String dateRecorded,
            personResponsibleId,
            recordedById,
            transactionType,
            transactionDetails;
    public boolean isIncome;

    public UnusaTransacton() {
    }

    public UnusaTransacton(int transactionAmount, String transactionId, int dueDate, int dueMonth, int dueYear, UnusaUser personResponsible, UnusaUser recordedBy, String dateRecorded, String personResponsibleId, String recordedById, String transactionType, String transactionDetails, boolean isIncome) {
        this.transactionAmount = transactionAmount;
        this.transactionId = transactionId;
        this.dueDate = dueDate;
        this.dueMonth = dueMonth;
        this.dueYear = dueYear;
        this.personResponsible = personResponsible;
        this.recordedBy = recordedBy;
        this.dateRecorded = dateRecorded;
        this.personResponsibleId = personResponsibleId;
        this.recordedById = recordedById;
        this.transactionType = transactionType;
        this.transactionDetails = transactionDetails;
        this.isIncome = isIncome;
    }
}
