workspace "ddd-hexagonal" "API de pedidos com DDD tático e arquitetura hexagonal" {

    !identifiers hierarchical

    model {
        cliente = person "Cliente" "Monta, fecha e cancela pedidos, com ou sem cupom de desconto."
        gestor = person "Gestor do catálogo" "Cadastra, consulta e desativa produtos."

        pedidos = softwareSystem "API de Pedidos" "Catálogo de produtos e pedidos com cupom de desconto." {

            api = container "Aplicação" "API REST com as regras de pedido e de catálogo." "Java 25, Spring Boot 4.1" {
                web = component "Controladores REST" "OrderController e ProductController: validam a entrada e traduzem HTTP em casos de uso." "Spring Web MVC" "Adaptador"
                erros = component "Tratamento de erros" "ApiExceptionHandler: regra violada vira 422, recurso ausente 404, entrada inválida 400, sem detalhes internos." "Problem Details (RFC 9457)" "Adaptador"
                seguranca = component "Filtro de segurança" "SecurityConfig: API stateless e cabeçalhos de segurança." "Spring Security" "Adaptador"
                casosDeUso = component "Serviços de aplicação" "OrderApplicationService e CatalogApplicationService: orquestram os casos de uso das portas de entrada." "Java puro" "Aplicação"
                dominio = component "Modelo de domínio" "Order, OrderItem, Product, Coupon, Money, Quantity e ProductOrderingService: as regras de negócio." "Java puro" "Domínio"
                persistencia = component "Adaptador de persistência" "Implementa OrderRepository e ProductRepository com Spring Data JPA." "Spring Data JPA, Hibernate" "Adaptador"
                cupons = component "Catálogo de cupons" "InMemoryCouponCatalog: implementa CouponCatalog com os cupons vigentes." "Java" "Adaptador"
                eventos = component "Publicador de eventos" "SpringDomainEventPublisher e DomainEventLogger: publicam e registram os eventos de domínio." "Spring Events" "Adaptador"
                migracoes = component "Migrações" "Criam e evoluem o esquema com o usuário dono das tabelas." "Flyway" "Adaptador"
            }

            banco = container "Banco de pedidos" "Produtos, pedidos, itens e cupom aplicado." "PostgreSQL 18" "Banco"
        }

        cliente -> pedidos "Monta e fecha pedidos" "HTTPS/JSON"
        gestor -> pedidos "Mantém o catálogo" "HTTPS/JSON"

        cliente -> pedidos.api "Chama /orders" "HTTP/JSON"
        gestor -> pedidos.api "Chama /products" "HTTP/JSON"
        pedidos.api -> pedidos.banco "Lê e grava como orders_app; migra como orders_owner" "JDBC"

        cliente -> pedidos.api.seguranca "Envia requisições" "HTTP/JSON"
        gestor -> pedidos.api.seguranca "Envia requisições" "HTTP/JSON"
        pedidos.api.seguranca -> pedidos.api.web "Repassa a requisição"
        pedidos.api.web -> pedidos.api.casosDeUso "Chama as portas de entrada"
        pedidos.api.web -> pedidos.api.erros "Delega exceções"
        pedidos.api.casosDeUso -> pedidos.api.dominio "Executa as regras"
        pedidos.api.casosDeUso -> pedidos.api.persistencia "Carrega e salva agregados" "Portas de repositório"
        pedidos.api.casosDeUso -> pedidos.api.cupons "Busca cupom pelo código" "CouponCatalog"
        pedidos.api.casosDeUso -> pedidos.api.eventos "Publica eventos depois de salvar" "DomainEventPublisher"
        pedidos.api.persistencia -> pedidos.banco "SQL parametrizado" "JDBC"
        pedidos.api.migracoes -> pedidos.banco "DDL na subida" "JDBC"

        local = deploymentEnvironment "Local (Docker Compose)" {
            deploymentNode "Máquina do desenvolvedor" "" "Docker Engine" {
                deploymentNode "app" "Nas redes frontend e backend; porta 8080 publicada; processo sem root." "eclipse-temurin:25-jre" {
                    containerInstance pedidos.api
                }
                deploymentNode "postgres" "Só na rede backend (internal), sem porta publicada." "postgres:18-alpine" {
                    containerInstance pedidos.banco
                }
            }
        }
    }

    views {
        systemContext pedidos "Contexto" "C1: quem usa a API de Pedidos." {
            include *
        }

        container pedidos "Conteineres" "C2: os blocos que executam e guardam dados." {
            include *
        }

        component pedidos.api "Componentes" "C3: os componentes da aplicação, organizados como portas e adaptadores." {
            include *
        }

        dynamic pedidos.api "AplicarCupom" "Aplicar cupom a um pedido em rascunho." {
            pedidos.api.web -> pedidos.api.casosDeUso "applyCoupon(orderId, code)"
            pedidos.api.casosDeUso -> pedidos.api.persistencia "findById(orderId)"
            pedidos.api.casosDeUso -> pedidos.api.cupons "findByCode(code)"
            pedidos.api.casosDeUso -> pedidos.api.dominio "order.applyCoupon(coupon)"
            pedidos.api.casosDeUso -> pedidos.api.persistencia "save(order)"
            pedidos.api.casosDeUso -> pedidos.api.eventos "publish(order.pullEvents())"
        }

        deployment pedidos local "Implantacao" "Implantação local com Docker Compose." {
            include *
        }

        styles {
            element "Person" {
                shape Person
                background #08427b
                color #ffffff
            }
            element "Software System" {
                background #1168bd
                color #ffffff
            }
            element "Container" {
                background #438dd5
                color #ffffff
            }
            element "Banco" {
                shape Cylinder
            }
            element "Component" {
                background #85bbf0
                color #000000
            }
            element "Domínio" {
                background #f5da81
            }
            element "Aplicação" {
                background #c6e3a6
            }
        }
    }
}
