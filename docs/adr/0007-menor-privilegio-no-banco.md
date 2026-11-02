# ADR-0007: Usuários separados para migrar e para operar o banco

- **Data:** 01/11/2026
- **Status:** Aceita
- **Dono:** Gean Ribeiro

## Contexto

Com um único usuário dono do banco, uma falha na aplicação (uma injeção de SQL que escape da validação, por exemplo) poderia apagar ou alterar tabelas. Pelo princípio do menor privilégio, cada parte do sistema deve ter só as permissões de que precisa.

## Decisão

O banco tem dois papéis, criados em `db/init/01-roles.sql`:

- `orders_owner`, dono do esquema, usado só pelo Flyway na subida (`SPRING_FLYWAY_USER`);
- `orders_app`, usado pela aplicação, com `SELECT`, `INSERT`, `UPDATE` e `DELETE` nas tabelas e nada além disso.

O PostgreSQL fica numa rede Docker interna, sem porta publicada; só o container da aplicação o alcança. `make privileges` comprova que `CREATE`, `ALTER` e `DROP` são negados ao `orders_app`.

## Alternativas consideradas

- **Um usuário só, dono de tudo**: mais simples, mas qualquer falha na aplicação vira poder de DDL.
- **Migrações num pipeline separado**: separa ainda mais as credenciais, mas exige uma etapa de implantação que este lab não tem.

## Consequências

- Um ataque pela aplicação não consegue mudar a estrutura do banco.
- A aplicação precisa de duas credenciais na subida.
- Toda tabela nova precisa ser criada pelo `orders_owner` para herdar as permissões padrão do `orders_app`.
