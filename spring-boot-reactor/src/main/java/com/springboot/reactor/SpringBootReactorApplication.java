package com.springboot.reactor;

import com.springboot.reactor.models.Comentarios;
import com.springboot.reactor.models.Usuario;
import com.springboot.reactor.models.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        this.ejemploContraPresion();
//        this.ejemploIntervalDesdeCreate();
//        this.ejemploIntervalInfinito();
//        this.ejemploDelayElements();
//        this.ejemploInterval();
//        this.ejemploZipWithRangos();
//        this.ejemploUsuarioComentariosZipWithConMap();
//        this.ejemploUsuarioComentariosZipWith();
//        this.ejemploUsuarioComentariosFlatMap();
//        this.ejemploFluxToMono();
//        this.ejemploFlatString();
//        this.ejemploFlatMap();
//        this.ejemploIterable();
    }

    public void ejemploContraPresion() { //la contrapresion es una técnica para manejar el flujo de datos asíncrono y evitar problemas(como el desbordamiento de memoria o el procesamiento excesivo) que pueden surgir cuando los productores de datos generan información más rápido de lo que los consumidores pueden procesar.
        Flux.range(1, 10)
                .log()
//                .limitRate(5) //acá lo maneja de forma más automática, y ve cual seria la cantidad adecuada a enviar de lotes, por ejm de 5 en 5 o de 4 en 4, pero si se quiere manejar de manera propia seria en el subscribe()
                .subscribe(new Subscriber<Integer>() { //esta seria una forma y la otra es con limitRate

                    private Subscription s;
                    private Integer limite = 2; //con esto ya no le solicita al productor todos los elementos de uno solo, sino que lo solicita de acuerdo al limite es decir por lotes
                    private Integer consumido = 0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(limite);
                    }

                    @Override
                    public void onNext(Integer i) {
                        log.info(i.toString());

                        consumido++;
                        if (consumido == limite) {
                            consumido = 0;
                            s.request(limite);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void ejemploIntervalDesdeCreate() {
        Flux.create(emitter -> {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        private Integer contador = 0;

                        @Override
                        public void run() {
                            emitter.next(++contador);

                            if (contador == 10) {
                                timer.cancel();
                                emitter.complete();
                            }

                            if (contador == 5) {
                                timer.cancel();
                                emitter.error(new InterruptedException("Error, se ha detenido el flux en 5"));
                            }
                        }
                    }, 1000, 1000);
                })
                /*.doOnNext(next -> log.info(next.toString())) //esto se puede meter en el subscribe
                .doOnComplete(() -> log.info("Completado"))*/ //esto se puede meter en el subscribe
                .subscribe(
                        next -> log.info(next.toString()),
                        error -> log.error(error.getMessage()),
                        () -> log.info("Completado")
                );
    }

    public void ejemploIntervalInfinito() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(latch::countDown) //este método siempre se va a ejecutar falle o no falle, si ocurre un error va a terminar el observable(se invoca a este método y decrementa su contador a 0) por lo tanto termina el proceso y el await se libera, de lo contrario si nunca ocurre un error el doOnTerminate se va a ejecutar al finalizar el observable hasta que se emita el ultimo item del flujo.
                .flatMap(i -> { //flatMap aplana todos los maps en uno solo
                    if (i >= 5) {
                        return Flux.error(new InterruptedException("Solo hasta 5"));
                    }

                    return Flux.just(i);
                })
                .map(i -> "Hola " + i)
                .retry(2) //intentar 2 veces despues de que falla
//                .doOnNext(log::info)
                .subscribe(log::info, error -> log.error(error.getMessage())); //utilizamos el método doOnTerminate antes del map para que bloquee el hilo(flujo) y le enviamos el CountDownLatch que estará esperando a que el contador llegue a 0 y cuando llegue a 0 activará con el await(que nunca pasará porque el contador se irá incrementando) el throws InterruptedException

        latch.await();
    }

    public void ejemploDelayElements() {
        Flux<Integer> rango = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(3))
                .doOnNext(i -> log.info(i.toString()));

//        rango.subscribe();
        rango.blockLast();  //este método también se suscribe pero bloqueando, hace que sea bloqueante, es decir que la aplicacion no termine hasta que se termine todo el rango

    }

    public void ejemploInterval() {
        Flux<Integer> rango = Flux.range(1, 12);
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(3));

        rango.zipWith(retraso, (ra, re) -> ra)
                .doOnNext(i -> log.info(i.toString()))
//                .subscribe()
                .blockLast();  //este método también se suscribe pero bloqueando, hace que sea bloqueante, es decir que la aplicacion no termine hasta que se termine todo el rango

    }

    public void ejemploZipWithRangos() {
        Flux<Integer> rango = Flux.range(0, 4);
        Flux.just(1, 2, 3, 4, 5)
                .map(n -> n * 2)
                .zipWith(rango, (num1, num2) -> String.format("Primer flux: %d, Segundo flux: %d", num1, num2))
                .subscribe(log::info); //nomas imprime hasta el num1: 4 porque el range brinda empieza en 0 y solo brinda 4 elementos
    }

    public void ejemploUsuarioComentariosZipWithConMap() {
        Flux<Usuario> usuarioFlux = Flux.fromStream(() -> Stream.of(
                new Usuario("John", "Doe"),
                new Usuario("Jane", "Smith")
        ));
        Flux<Comentarios> comentariosFlux = Flux.fromStream(() -> {
            Comentarios comentarios1 = new Comentarios();
            comentarios1.addComentarios("Hola 1.1");
            comentarios1.addComentarios("Hola 1.2");
            comentarios1.addComentarios("Hola 1.3");
            comentarios1.addComentarios("Hola 1.4");

            Comentarios comentarios2 = new Comentarios();
            comentarios2.addComentarios("Hola 2.1");
            comentarios2.addComentarios("Hola 2.2");

            Comentarios comentarios3 = new Comentarios();
            comentarios3.addComentarios("Hola 3.1");
            comentarios3.addComentarios("Hola 3.2");
            comentarios3.addComentarios("Hola 3.3");

            return Stream.of(comentarios1, comentarios2, comentarios3);
        });

        // Combinar los fluxes de usuarios y comentarios
        Flux<UsuarioComentarios> usuarioComentariosFlux = usuarioFlux.zipWith(comentariosFlux).map(objects -> {
            Usuario u = objects.getT1();
            Comentarios c = objects.getT2();

            return new UsuarioComentarios(u, c);
        });
        usuarioComentariosFlux.subscribe(uc -> log.info(uc.toString()));
    }

    public void ejemploUsuarioComentariosZipWith() {
        Flux<Usuario> usuarioFlux = Flux.fromStream(() -> Stream.of(
                new Usuario("John", "Doe"),
                new Usuario("Jane", "Smith")
        ));
        Flux<Comentarios> comentariosFlux = Flux.fromStream(() -> {
            Comentarios comentarios1 = new Comentarios();
            comentarios1.addComentarios("Hola 1.1");
            comentarios1.addComentarios("Hola 1.2");
            comentarios1.addComentarios("Hola 1.3");
            comentarios1.addComentarios("Hola 1.4");

            Comentarios comentarios2 = new Comentarios();
            comentarios2.addComentarios("Hola 2.1");
            comentarios2.addComentarios("Hola 2.2");

            Comentarios comentarios3 = new Comentarios();
            comentarios3.addComentarios("Hola 3.1");
            comentarios3.addComentarios("Hola 3.2");
            comentarios3.addComentarios("Hola 3.3");

            return Stream.of(comentarios1, comentarios2, comentarios3);
        });

        // Combinar los fluxes de usuarios y comentarios
        Flux<UsuarioComentarios> usuarioComentariosFlux = usuarioFlux.zipWith(comentariosFlux, UsuarioComentarios::new);
        usuarioComentariosFlux.subscribe(uc -> log.info(uc.toString()));
    }

    public void ejemploUsuarioComentariosFlatMap() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));
        Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();

            comentarios.addComentarios("Hola 1");
            comentarios.addComentarios("Hola 2");
            comentarios.addComentarios("Hola 3");
            comentarios.addComentarios("Hola 4");

            return comentarios;
        });

        Mono<UsuarioComentarios> usuarioMono2 = usuarioMono.flatMap(u -> comentariosMono.map(c -> new UsuarioComentarios(u, c))); //flatMap aplana todos los maps en uno solo
        usuarioMono2.subscribe(uc -> log.info("forma 1: " + uc.toString()));

        Mono<UsuarioComentarios> usuarioMono3 = usuarioMono.zipWith(comentariosMono, (usuario, comentarios) -> new UsuarioComentarios(usuario, comentarios));
        usuarioMono3.subscribe(uc -> log.info("forma 2: " + uc.toString()));

        /*
        //esto no se debe hacer ya que son inmutables
        usuarioMono.flatMap(u -> comentariosMono.map(c -> new UsuarioComentarios(u, c)))
                .subscribe(uc -> log.info(uc.toString()));
        */
    }

    public void ejemploFluxToMono() throws Exception {

        List<Usuario> usuariosList = new ArrayList<>();
        usuariosList.add(new Usuario("Carlos", "Juarez"));
        usuariosList.add(new Usuario("Andres", "Guzman"));
        usuariosList.add(new Usuario("Carlos", "Chong"));
        usuariosList.add(new Usuario("Adela", "Burneo"));
        usuariosList.add(new Usuario("Jackie", "Chong"));
        usuariosList.add(new Usuario("Carlos", "Antonio"));

        Flux.fromIterable(usuariosList)
                .collectList()
//                .subscribe(usuario -> log.info(usuario.toString()));
                .subscribe(lista -> lista.forEach(item -> log.info(item.toString())));
    }

    public void ejemploFlatString() throws Exception {

        List<Usuario> usuariosList = new ArrayList<>();
        usuariosList.add(new Usuario("Carlos", "Juarez"));
        usuariosList.add(new Usuario("Andres", "Guzman"));
        usuariosList.add(new Usuario("Carlos", "Chong"));
        usuariosList.add(new Usuario("Adela", "Burneo"));
        usuariosList.add(new Usuario("Jackie", "Chong"));
        usuariosList.add(new Usuario("Carlos", "Antonio"));

        //Creando Observable
        Flux.fromIterable(usuariosList).map(usuario -> usuario.getNombre().toUpperCase() + " " + usuario.getApellidos().toUpperCase())
                .flatMap(usuario -> {
                    if (usuario.toLowerCase().contains("carlos")) {
                        return Mono.just(usuario);
                    }

                    return Mono.empty();
                }) //basicamente lo que está haciendo es que aplana todos mis Mono separados que retorna en un solo Flux
                .map(usuario -> usuario.split(" ")[0].toLowerCase() + " " + usuario.split(" ")[1])
                .subscribe(log::info);
    }

    public void ejemploFlatMap() throws Exception {
        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Carlos Juarez");
        usuariosList.add("Andres Guzman");
        usuariosList.add("Carlos Chong");
        usuariosList.add("Adela Burneo");
        usuariosList.add("Jackie Chong");
        usuariosList.add("Carlos Antonio");

        //Creando Observable
        Flux.fromIterable(usuariosList).map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
                .flatMap(usuario -> {
                    if (usuario.getNombre().equalsIgnoreCase("carlos")) {
                        return Mono.just(usuario);
                    }

                    return Mono.empty();
                }) //basicamente lo que está haciendo es que aplana todos mis Mono separados que retorna en un solo Flux
                .map(usuario -> new Usuario(usuario.getNombre().toLowerCase(), usuario.getApellidos()))
                .subscribe(
                        usuario -> log.info(usuario.toString())
                );
    }

    public void ejemploIterable() throws Exception {

        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Carlos Juarez");
        usuariosList.add("Andres Guzman");
        usuariosList.add("Carlos Chong");
        usuariosList.add("Adela Burneo");
        usuariosList.add("Jackie Chong");
        usuariosList.add("Carlos Antonio");

        //Creando Observable
        Flux<String> nombres = Flux.fromIterable(usuariosList);
//        Flux<String> nombres = Flux.just("Carlos Juarez", "Andres Guzman", "Carlos Chong", "Maria Zapata", "Adela Burneo", "Jackie Chong", "Carlos Antonio");
        Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
                .filter(usuario -> usuario.getNombre().equalsIgnoreCase("carlos"))
                .doOnNext(usuario -> {
                    if (usuario == null) {
                        throw new RuntimeException("Nombres no pueden ser vacios");
                    }
                })
                .map(usuario -> new Usuario(usuario.getNombre().toLowerCase(), usuario.getApellidos()));

//        nombres.subscribe(
        usuarios.subscribe(
                usuario -> log.info(usuario.toString()), //Consumer
                error -> log.error(error.getMessage()), //errorConsumer
                new Runnable() {
                    @Override
                    public void run() {
                        log.info("Ha finalizado la ejecucion del observable con éxito");
                    }
                }
        );
    }
}
