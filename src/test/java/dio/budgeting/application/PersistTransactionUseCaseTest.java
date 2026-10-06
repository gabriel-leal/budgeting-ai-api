package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistTransactionUseCaseTest {

    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
    private final PersistTransactionUseCase useCase = new PersistTransactionUseCase(repository);

    @Test
    void shouldPersistAValidTransactionAndConvertCentsToReais() {
        var output = useCase.execute(new PersistTransactionInput("  Supermercado  ", 2590, Category.GROCERIES));

        assertEquals("Supermercado", output.description());
        assertEquals("GROCERIES", output.category());
        assertEquals(25.90, output.value());
        assertEquals(1, repository.transactions.size());
    }

    @Test
    void shouldRejectBlankDescription() {
        var exception = assertThrows(InvalidTransactionException.class,
                () -> useCase.execute(new PersistTransactionInput("   ", 2590, Category.GROCERIES)));

        assertEquals("A descrição da transação é obrigatória.", exception.getMessage());
        assertEquals(0, repository.transactions.size());
    }

    @Test
    void shouldRejectNonPositiveAmount() {
        var exception = assertThrows(InvalidTransactionException.class,
                () -> useCase.execute(new PersistTransactionInput("Supermercado", 0, Category.GROCERIES)));

        assertEquals("O valor da transação deve ser maior que zero.", exception.getMessage());
        assertEquals(0, repository.transactions.size());
    }

    @Test
    void shouldRejectMissingCategory() {
        var exception = assertThrows(InvalidTransactionException.class,
                () -> useCase.execute(new PersistTransactionInput("Supermercado", 2590, null)));

        assertEquals("A categoria da transação é obrigatória.", exception.getMessage());
        assertEquals(0, repository.transactions.size());
    }

    private static class InMemoryTransactionRepository implements TransactionRepository {
        private final List<Transaction> transactions = new ArrayList<>();

        @Override
        public Transaction save(Transaction transaction) {
            transactions.add(transaction);
            return transaction;
        }

        @Override
        public List<Transaction> findAllByCategory(Category category) {
            return transactions.stream()
                    .filter(transaction -> transaction.getCategory() == category)
                    .toList();
        }
    }
}
