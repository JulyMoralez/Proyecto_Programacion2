import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

public class Aerolinea implements IAerolinea {
    private String nombre;
    private String cuit;
    private Map<String, Aeropuerto> aeropuertos;
    private Map<String, Vuelo> Vuelos;
    private Map<Integer, Cliente> clientes;
    private Map<String, Double> recaudacionPorDestino;

    // Funcion 1
    public Aerolinea(String nombre, String cuit) {
        validacionNombreCuit(nombre, cuit);
        this.nombre = nombre;
        this.cuit = cuit;
        this.aeropuertos = new HashMap<>();
        this.clientes = new HashMap<>();
        this.Vuelos = new HashMap<>();
        this.recaudacionPorDestino = new HashMap<>();
    }

    // Funcion 2
    public void registrarCliente(int dni, String nombre, String telefono) {
        if (clientes.containsKey(dni)) {
            throw new RuntimeErrorException(null, "Cliente ya existente");
        }
        Cliente nuevoCliente = new Cliente(dni, nombre, telefono);

        clientes.put(dni, nuevoCliente);
    }

    // Funcion 3
    public void registrarAeropuerto(String nombre, String pais, String provincia, String direccion) {
        // si ya existe ese aeropuerto en aerpuertos no se hace
        if (aeropuertos.containsKey(nombre)) {
            throw new RuntimeErrorException(null, "Aeropuerto ya existente");
        }

        Aeropuerto nuevoAeropuerto = new Aeropuerto(nombre, pais, provincia, direccion);
        aeropuertos.put(nombre, nuevoAeropuerto);
    }

    // Funcion 4
    public String registrarVueloPublicoNacional(String origen, String destino, String fecha, int tripulantes,
            double valorRefrigerio, double[] precios, int[] cantAsientos) {
        if (!aeropuertos.containsKey(origen) || !aeropuertos.containsKey(destino)) {
            throw new RuntimeException("Origen o destino no registrados");
        }
        if (!aeropuertos.get(origen).getPais().equals("Argentina") ||
                !aeropuertos.get(destino).getPais().equals("Argentina")) {
            throw new RuntimeException("Los aeropuertos deben ser nacionales");
        }

        if (origen == destino) {
            throw new RuntimeErrorException(null, "Origen y destino no pueden ser iguales");
        }
        if (!fechaValida(fecha)) {
            throw new RuntimeErrorException(null, "Fecha invalida");
        }
        VueloNacional nuevoVuelo = new VueloNacional(origen, destino, fecha, tripulantes, valorRefrigerio, precios,
                cantAsientos);
        recaudacionPorDestino.put(destino,
                recaudacionPorDestino.getOrDefault(destino, 0.0) + nuevoVuelo.getRecaudacionTotal());
        String codigoVuelo = nuevoVuelo.getCodigo();
        Vuelos.put(codigoVuelo, nuevoVuelo);

        return codigoVuelo;
    }

    // Funcion 5
    public String registrarVueloPublicoInternacional(String origen, String destino, String fecha, int tripulantes,
            double valorRefrigerio, int cantRefrigerios, double[] precios, int[] cantAsientos, String[] escalas) {
        if (!aeropuertos.containsKey(origen) || !aeropuertos.containsKey(destino)) {
            throw new RuntimeException("Origen o destino no registrados");
        }
        validacionEscalas(escalas);
        if (!fechaValida(fecha)) {
            throw new RuntimeErrorException(null, "Fecha invalida");
        }
        if (origen == destino) {
            throw new RuntimeErrorException(null, "Origen y destino no pueden ser iguales");
        }
        VueloInternacional nuevoVuelo = new VueloInternacional(origen, destino, fecha, tripulantes, valorRefrigerio,
                cantRefrigerios, precios, cantAsientos, escalas);
        recaudacionPorDestino.put(destino,
                recaudacionPorDestino.getOrDefault(destino, 0.0) + nuevoVuelo.getRecaudacionTotal());
        String codigoVuelo = nuevoVuelo.getCodigo();
        Vuelos.put(codigoVuelo, nuevoVuelo);
        return codigoVuelo;
    }

    // Funcion 6 y 10
    public String VenderVueloPrivado(String origen, String destino, String fecha, int tripulantes, double precio,
            int dniComprador, int[] acompaniantes) {
        validacionAcompaniantes(acompaniantes);
        validacionDniComprador(dniComprador);
        for (int i = 1; i < acompaniantes.length; i++) {
            if (!clientes.containsKey(acompaniantes[i])) {
                throw new RuntimeErrorException(null, "Error en los acompañantes, inexistentes o datos invalidos");
            }
        }
        if (!fechaValida(fecha)) {
            throw new RuntimeErrorException(null, "Fecha invalida");
        }
        if (origen == destino) {
            throw new RuntimeErrorException(null, "Origen y destino no pueden ser iguales");
        }

        VueloPrivado nuevoVuelo = new VueloPrivado(origen, destino, fecha, tripulantes, precio, dniComprador,
                acompaniantes);
        recaudacionPorDestino.put(destino,
                recaudacionPorDestino.getOrDefault(destino, 0.0) + nuevoVuelo.getRecaudacionTotal());

        String codigoVuelo = nuevoVuelo.getCodigo();
        Vuelos.put(codigoVuelo, nuevoVuelo);
        return codigoVuelo;

    }

    // Funcion 7
    public Map<Integer, String> asientosDisponibles(String codVuelo) {
        Vuelo vuelo = Vuelos.get(codVuelo);
        if (vuelo == null) {
            throw new RuntimeException("El vuelo no existe");
        }
        if (vuelo instanceof VueloInternacional) {
            VueloInternacional vueloInternacional = (VueloInternacional) vuelo;
            return vueloInternacional.getAsientosDisponibles();
        } else if (vuelo instanceof VueloNacional) {
            VueloNacional vueloNacional = (VueloNacional) vuelo;
            return vueloNacional.getAsientosDisponibles();
        } else if (vuelo instanceof VueloPrivado) {
            throw new RuntimeException("El vuelo no tiene acceso a los asientos");
        } else {
            throw new RuntimeException("El vuelo no existe");
        }
    }

    // Funcion 8 y 9
    public int venderPasaje(int dni, String codVuelo, int nroAsiento, boolean aOcupar) {
        if (clientes.get(dni) == null) {
            throw new RuntimeException("El cliente no está registrado");
        }
        if (clientes.get(dni).esPasajero() == true) {
            if (aOcupar == true) {
                throw new RuntimeException("El cliente ya tiene asiento designado");
            }
        }
        Vuelo vuelo = Vuelos.get(codVuelo);
        if (vuelo == null) {
            throw new RuntimeException("El vuelo no existe");
        }
        int resultado;
        if (vuelo instanceof VueloInternacional) {

            VueloInternacional vueloInternacional = (VueloInternacional) vuelo;
            resultado = vueloInternacional.venderPasaje(dni, nroAsiento, aOcupar, codVuelo);

            String clase = vueloInternacional.determinarClase(nroAsiento);
            double precioPasaje = vueloInternacional.getClaseSeccion(clase);
            recaudacionPorDestino.put(vueloInternacional.getDestino(),
                    recaudacionPorDestino.get(vueloInternacional.getDestino()) + precioPasaje);
            if (resultado <= 0) {
                throw new RuntimeException("Hubo un error en la designación del asiento");
            }
            if (clientes.get(dni).esPasajero() == false && aOcupar == true) {
                clientes.get(dni).cambiarEstado(true);
            } else if (aOcupar == false && clientes.get(dni).esPasajero() == false) {
                clientes.get(dni).cambiarEstado(false);
            } else if (aOcupar == false && clientes.get(dni).esPasajero() == true) {
                clientes.get(dni).cambiarEstado(true);
            } else {
                throw new RuntimeException("Hubo un error en la designación del asiento");
            }

        } else if (vuelo instanceof VueloNacional) {
            VueloNacional vueloNacional = (VueloNacional) vuelo;
            resultado = vueloNacional.venderPasaje(dni, nroAsiento, aOcupar, codVuelo);

            String clase = vueloNacional.determinarClase(nroAsiento);
            double precioPasaje = vueloNacional.getClaseSeccion(clase);
            recaudacionPorDestino.put(vueloNacional.getDestino(),
                    recaudacionPorDestino.get(vueloNacional.getDestino()) + precioPasaje);

            if (resultado <= 0) {
                throw new RuntimeException("Hubo un error en la designación del asiento");
            }

            if (clientes.get(dni).esPasajero() == false && aOcupar == true) {
                clientes.get(dni).cambiarEstado(true);
            } else if (aOcupar == false && clientes.get(dni).esPasajero() == false) {
                clientes.get(dni).cambiarEstado(false);
            } else if (aOcupar == false && clientes.get(dni).esPasajero() == true) {
                clientes.get(dni).cambiarEstado(true);
            } else {
                throw new RuntimeException("Hubo un error en la designación del asiento");
            }
        } else {
            throw new RuntimeException("El vuelo no forma parte de las clases definidas");
        }
        return resultado;
    }

    // Funcion 11
    public List<String> consultarVuelosSimilares(String origen, String destino, String Fecha) {
        List<String> resultado = new ArrayList<>();
        for (Vuelo vuelo : Vuelos.values()) {
            if (vuelo instanceof VueloInternacional) {
                VueloInternacional vueloInternacional = (VueloInternacional) vuelo;

                if (vueloInternacional.getOrigen().equals(origen) &&
                        vueloInternacional.getDestino().equals(destino) && vueloInternacional.fechaValida(Fecha)) {
                    resultado.add(vueloInternacional.getCodigo());
                }
            } else if (vuelo instanceof VueloNacional) {
                VueloNacional vueloNacional = (VueloNacional) vuelo;
                if (vueloNacional.getOrigen().equals(origen) && vueloNacional.getDestino().equals(destino)
                        && vueloNacional.fechaValida(Fecha)) {
                    resultado.add(vueloNacional.getCodigo());
                }
            }
        }
        return resultado;
    }

    // Funcion 12-A Complejidad O(1)
    public void cancelarPasaje(int dni, String codVuelo, int nroAsiento) {
        Cliente cl = clientes.get(dni);
        if (cl == null) {
            throw new RuntimeException("El cliente no está registrado");
        } // 1
        Vuelo v = Vuelos.get(codVuelo);
        if (v == null) {
            throw new RuntimeException("El vuelo no existe");
        } // 1
        if (v instanceof VueloInternacional) {
            VueloInternacional vueloInternacional = (VueloInternacional) v;

            if (vueloInternacional.tienePasaje(dni)) {
                vueloInternacional.cancelarPasaje(dni, nroAsiento);
                String clase = vueloInternacional.determinarClase(nroAsiento);
                double precioPasaje = vueloInternacional.getClaseSeccion(clase);
                recaudacionPorDestino.put(vueloInternacional.getDestino(),
                        recaudacionPorDestino.get(vueloInternacional.getDestino()) - precioPasaje);
                if (cl.esPasajero()) {
                    cl.cambiarEstado(false);
                } //
            } else {
                throw new RuntimeException("El pasaje no existe");
            }
        } else if (v instanceof VueloNacional) {
            VueloNacional vueloNacional = (VueloNacional) v;

            if (vueloNacional.tienePasaje(dni)) {
                vueloNacional.cancelarPasaje(dni, nroAsiento);
                String clase = vueloNacional.determinarClase(nroAsiento);
                double precioPasaje = vueloNacional.getClaseSeccion(clase);
                recaudacionPorDestino.put(vueloNacional.getDestino(),
                        recaudacionPorDestino.get(vueloNacional.getDestino()) - precioPasaje);
                if (cl.esPasajero()) {
                    cl.cambiarEstado(false);
                }
            } else {
                throw new RuntimeException("El pasaje no existe");
            }
        } else {
            throw new RuntimeException("El pasaje no existe o no se tiene acceso");
        }
    }

    // Funcion 12-B NO Complejidad O(1)
    public void cancelarPasaje(int dni, int codPasaje) {
        Cliente cl = clientes.get(dni);
        if (cl == null) {
            throw new RuntimeException("El cliente no está registrado");
        }
        for (Vuelo vuelo : Vuelos.values()) {
            if (vuelo instanceof VueloNacional && ((VueloNacional) vuelo).tienePasaje(dni)) {
                VueloNacional vueloNacional = (VueloNacional) vuelo;

                int nroAsiento = vueloNacional.getPasajerosporCodPasaje().get(codPasaje).getNroAsiento();
                vueloNacional.cancelarPasaje(dni, nroAsiento, codPasaje);
                String clase = vueloNacional.determinarClase(nroAsiento);
                double precioPasaje = vueloNacional.getClaseSeccion(clase);
                recaudacionPorDestino.put(vueloNacional.getDestino(),
                        recaudacionPorDestino.get(vueloNacional.getDestino()) - precioPasaje);
                if (cl.esPasajero()) {
                    cl.cambiarEstado(false);
                }
                return;

            } else if (vuelo instanceof VueloInternacional
                    && ((VueloInternacional) vuelo).tienePasaje(dni)) {
                VueloInternacional vueloInternacional = (VueloInternacional) vuelo;

                int nroAsiento = vueloInternacional.getPasajerosporCodPasaje().get(codPasaje).getNroAsiento();
                vueloInternacional.cancelarPasaje(dni, nroAsiento, codPasaje);
                String clase = vueloInternacional.determinarClase(nroAsiento);
                double precioPasaje = vueloInternacional.getClaseSeccion(clase);
                recaudacionPorDestino.put(vueloInternacional.getDestino(),
                        recaudacionPorDestino.get(vueloInternacional.getDestino()) - precioPasaje);
                if (cl.esPasajero()) {
                    cl.cambiarEstado(false);
                }
                return;
            }
        }
    }

    // Funcion 13
    public List<String> cancelarVuelo(String codVuelo) {
        List<String> listaPasajerosReprogramados = new ArrayList<>();
        Vuelo vuelo = Vuelos.get(codVuelo);

        if (vuelo == null) {
            throw new RuntimeException("El vuelo no existe");
        }

        if (!(vuelo instanceof VueloPublico)) {
            throw new RuntimeException("No se puede cancelar un vuelo privado");
        }

        VueloPublico vueloPublico = (VueloPublico) vuelo;
        boolean encontroVueloValido = false;

        Map<Integer, Pasaje> pasajerosVuelo = new HashMap<>(vueloPublico.getPasajerosporCodPasaje());

        if (pasajerosVuelo.size() > 0) {
            for (Vuelo v : new ArrayList<>(Vuelos.values())) {
                if (v instanceof VueloNacional) {
                    VueloNacional vueloNacional = (VueloNacional) v;

                    if (vueloNacional != vueloPublico &&
                            vueloNacional.getOrigen().equals(vueloPublico.getOrigen()) &&
                            vueloNacional.getDestino().equals(vueloPublico.getDestino()) &&
                            vueloNacional.fechaValida(vueloPublico.getFecha())) {
                        encontroVueloValido = true;

                        for (Pasaje p : pasajerosVuelo.values()) {
                            String telefonoCliente = clientes.get(p.getDni()).getTelefono();
                            String nombreCliente = clientes.get(p.getDni()).getNombre();
                            String codVueloNuevo = vueloNacional.getCodigo();
                            int nroAsiento = p.getNroAsiento();
                            if (vueloNacional.asignarAsiento(p.getDni(),
                                    nroAsiento,
                                    p.getClase(),
                                    p.getOcupado()) > 0) {
                                String clase = vueloNacional.determinarClase(nroAsiento);
                                double precioPasaje = vueloNacional.getClaseSeccion(clase);
                                recaudacionPorDestino.put(vueloNacional.getDestino(),
                                        recaudacionPorDestino.get(vueloNacional.getDestino()) + precioPasaje);
                                agregarPasajero(listaPasajerosReprogramados,
                                        p.getDni(),
                                        nombreCliente,
                                        telefonoCliente,
                                        codVueloNuevo);
                            } else {
                                agregarPasajero(listaPasajerosReprogramados,
                                        p.getDni(),
                                        nombreCliente,
                                        telefonoCliente,
                                        "CANCELADO");
                            }
                        }

                    }
                } else if (v instanceof VueloInternacional) {
                    VueloInternacional vueloInternacional = (VueloInternacional) v;

                    if (vueloInternacional != vueloPublico &&
                            vueloInternacional.getOrigen().equals(vueloPublico.getOrigen()) &&
                            vueloInternacional.getDestino().equals(vueloPublico.getDestino()) &&
                            vueloInternacional.fechaValida(vueloPublico.getFecha())) {

                        encontroVueloValido = true;
                        for (Pasaje p : pasajerosVuelo.values()) {
                            String telefonoCliente = clientes.get(p.getDni()).getTelefono();
                            String nombreCliente = clientes.get(p.getDni()).getNombre();
                            String codVueloNuevo = vueloInternacional.getCodigo();
                            int dniCliente = clientes.get(p.getDni()).getDni();
                            int nroAsiento = p.getNroAsiento();
                            if (vueloInternacional.asignarAsiento(p.getDni(),
                                    p.getNroAsiento(),
                                    p.getClase(),
                                    p.getOcupado()) > 0) {
                                String clase = vueloInternacional.determinarClase(nroAsiento);
                                double precioPasaje = vueloInternacional.getClaseSeccion(clase);
                                recaudacionPorDestino.put(vueloInternacional.getDestino(),
                                        recaudacionPorDestino.get(vueloInternacional.getDestino()) + precioPasaje);

                                agregarPasajero(listaPasajerosReprogramados,
                                        dniCliente,
                                        nombreCliente,
                                        telefonoCliente,
                                        codVueloNuevo);
                            } else {
                                agregarPasajero(listaPasajerosReprogramados,
                                        dniCliente,
                                        nombreCliente,
                                        telefonoCliente,
                                        "CANCELADO");
                            }
                        }

                    }
                } else {
                    System.out.println("No se pudo encontrar un vuelo válido para el pasajero");
                }

            }
            if (!encontroVueloValido) {
                for (Pasaje p : pasajerosVuelo.values()) {
                    String telefonoCliente = clientes.get(p.getDni()).getTelefono();
                    String nombreCliente = clientes.get(p.getDni()).getNombre();

                    agregarPasajero(listaPasajerosReprogramados,
                            p.getDni(),
                            nombreCliente,
                            telefonoCliente,
                            "CANCELADO");
                }

            }

            recaudacionPorDestino.put(vueloPublico.getDestino(),
                    recaudacionPorDestino.get(vueloPublico.getDestino())
                            - vueloPublico.getRecaudacionTotal());

            Vuelos.remove(codVuelo);

            return listaPasajerosReprogramados;
        } else {
            throw new RuntimeErrorException(null, "El vuelo no tiene pasajes vendidos");
        }

    }

    // Funcion 14
    public double totalRecaudado(String destino) {

        if (recaudacionPorDestino.get(destino) == null) {
            return 0.0;
        }

        double valor = recaudacionPorDestino.get(destino);
        return valor;
    }

    // Funcion 15
    public String detalleDeVuelo(String codVuelo) {
        if (codVuelo == null || codVuelo.isEmpty()) {
            throw new RuntimeErrorException(null, "codVuelo no puede ser nulo o vacio");
        }
        Vuelo v = Vuelos.get(codVuelo);
        if (v == null) {
            throw new RuntimeException("El vuelo no existe");
        }
        if (v instanceof VueloNacional) {
            VueloNacional vueloNacional = (VueloNacional) v;
            return crearSBVuelo(codVuelo, vueloNacional.getOrigen(), vueloNacional.getDestino(),
                    vueloNacional.getFecha(), vueloNacional.getTipoVuelo());
        } else if (v instanceof VueloInternacional) {
            VueloInternacional vueloInternacional = (VueloInternacional) v;
            return crearSBVuelo(codVuelo, vueloInternacional.getOrigen(), vueloInternacional.getDestino(),
                    vueloInternacional.getFecha(), vueloInternacional.getTipoVuelo());
        } else {
            VueloPrivado vueloPrivado = (VueloPrivado) v;
            return crearSBVuelo(codVuelo, vueloPrivado.getOrigen(), vueloPrivado.getDestino(), vueloPrivado.getFecha(),
                    vueloPrivado.getTipoVuelo());
        }
    }

    // *Funciones Auxiliares---------------------
    public void agregarPasajero(List<String> lista, int dni, String nombre,
            String telefono, String codVuelo) {
        StringBuilder sb = new StringBuilder();
        sb.append(dni).append(" - ")
                .append(nombre).append(" - ")
                .append(telefono).append(" - ")
                .append(codVuelo);
        lista.add(sb.toString());
    }

    public String crearSBVuelo(String codVuelo, String origen, String destino, String fecha, String tipoDeVuelo) {
        StringBuilder sb = new StringBuilder();
        sb.append(codVuelo).append(" - ").append(origen).append(" - ").append(destino).append(" - ").append(fecha)
                .append(" - ").append(tipoDeVuelo);
        return sb.toString();
    }

    public boolean fechaValida(String fecha) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/uuuu");
        LocalDate fechaVuelo = LocalDate.parse(fecha, formatter);
        LocalDate fechaHoy = LocalDate.now();
        return fechaVuelo.isAfter(fechaHoy);
    }

    // * IREPS------------------

    public void validacionEscalas(String[] escalas) {
        if (escalas.length > 0) {
            for (int i = 0; i < escalas.length; i++) {
                boolean escalasFalsa = true;
                if (escalas[i].length() < 3 && !aeropuertos.containsKey(escalas[i])) {
                    escalasFalsa = false;
                }
                if (!escalasFalsa) {
                    throw new RuntimeErrorException(null, "Las escalas son invalidas");
                }
            }
        }
    }

    public void validacionCantRefrigerios(int cantRefrigerios) {
        if (cantRefrigerios != 2) {
            throw new RuntimeException("La cantidad de refrigerios deben ser 2");
        }
    }

    public void validacionAcompaniantes(int[] acompaniantes) {
        for (int i = 0; i < acompaniantes.length; i++) {
            if (!clientes.containsKey(acompaniantes[i])) {
                throw new RuntimeException("El acompaniante no existe");
            }
        }
    }

    public void validacionDniComprador(int dniComprador) {
        if (!clientes.containsKey(dniComprador)) {
            throw new RuntimeException("El cliente no esta registrado");
        }
    }

    public void validacionNombreCuit(String nombre, String cuit) {
        if (nombre.isEmpty() || cuit.isEmpty() || nombre.length() < 3 || cuit.length() < 10) {
            throw new RuntimeException("El nombre y el cuit no pueden estar vacios");
        }
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getCuit() {
        return cuit;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Aerolinea: ").append(nombre).append("\n");
        sb.append("Cuit: ").append(cuit).append("\n");
        sb.append("Clientes: ").append(clientes.size()).append("\n");
        sb.append("Vuelos: ").append(Vuelos.size()).append("\n");
        return sb.toString();
    }
}
