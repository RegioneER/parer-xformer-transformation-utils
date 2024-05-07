# Xformer Transformation Utils

# Descrizione

Il seguente progetto è utilizzato come **dipendenza** interna per definire i processi di trasformazione attraverso il tool di Pentaho Kettle, è utilizzato quindi come libreria/dipendenza Maven (https://maven.apache.org/), dalle seguenti applicazioni: 
- Xformer Kettle Server

# Installazione

Come già specificato nel paragrafo precedente [Descrizione](# Descrizione) si tratta di un progetto di tipo "libreria", quindi un modulo applicativo utilizzato attraverso la definzione della dipendenza Maven secondo lo standard previsto (https://maven.apache.org/): 

```xml
<dependency>
  <groupId>it.eng.parer</groupId>
  <artifactId>xformer-transformations-utils-lib</artifactId>
  <version>$VERSIONE</version>
</dependency>
```

# Utilizzo

Il modulo contiene le specifiche logiche utilizzate nell'ambito dei processi di "data trasformation" all'interno di Xformer Kettle Server per le trasformazioni appositamente scritte per gli oggetti versati da Preingest.

# Supporto

Mantainer del progetto è [Engineering Ingegneria Informatica S.p.A.](https://www.eng.it/).

# Contributi

Se interessati a crontribuire alla crescita del progetto potete scrivere all'indirizzo email <a href="mailto:areasviluppoparer@regione.emilia-romagna.it">areasviluppoparer@regione.emilia-romagna.it</a>.

# Credits

Progetto di proprietà di [Regione Emilia-Romagna](https://www.regione.emilia-romagna.it/) sviluppato a cura di [Engineering Ingegneria Informatica S.p.A.](https://www.eng.it/).

# Licenza

Questo progetto è rilasciato sotto licenza GNU Affero General Public License v3.0 or later ([LICENSE.txt](LICENSE.txt)).

