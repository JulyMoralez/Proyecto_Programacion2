
public class VueloPrivado extends Vuelo {
    private double precio;
    private int dniComprador;
    private int[] acompaniantes;
    private static int contadorVuelos = 0;
    private String codigo;
    private int cantidadPasajerosPorJet = 15;
    private int cantJets;
    private double recaudacionTotal;

    public VueloPrivado(String origen, String destino, String fecha, int tripulantes, double precio,
            int dniComprador, int[] acompaniantes) {
        super(origen, destino, fecha, tripulantes);
        validacionOrigenDestinoNacional(origen, destino);

        this.precio = precio;
        this.dniComprador = dniComprador;
        this.acompaniantes = acompaniantes;
        this.codigo = generarCodigoVuelo() + "-PRI";
        this.cantJets = cantJets();
        this.recaudacionTotal = calcularRecaudacionTotal();
    }

    private int cantJets() {
        int cantidad = acompaniantes.length;
        int jets = cantidad / cantidadPasajerosPorJet;
        if (cantidad % cantidadPasajerosPorJet >= 0) {
            jets++;
        }
        return jets;
    }

    private double calcularRecaudacionTotal() {
        double precioBase = precio * cantJets;
        return precioBase * 1.3;
    }

    private static String generarCodigoVuelo() {
        contadorVuelos++;
        return String.valueOf(contadorVuelos);
    }

    public void validacionOrigenDestinoNacional(String origen, String destino) {
        if (origen == null || origen.isEmpty() || destino == null || destino.isEmpty()) {
            throw new RuntimeException("El origen y destino no pueden ser nulos o vacíos");
        }
    }

    // GETTERS
    public String getCodigo() {
        return codigo;
    }

    public int getDniComprador() {
        return dniComprador;
    }

    public int[] getAcompaniantes() {
        return acompaniantes;
    }

    public int getTotalPasajeros() {
        return acompaniantes.length + 1;
    }

    public double getRecaudacionTotal() {
        return this.recaudacionTotal;
    }

    public int getCantJets() {
        return cantJets;
    }

    public String getTipoVuelo() {
        return "PRIVADO" + " (" + cantJets + ")";
    }

}