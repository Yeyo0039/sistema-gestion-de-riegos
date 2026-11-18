// Punto de composición del frontend: inicializa estado, navegación y paneles.
import { checkBackendStatus } from "./backend-status.js";
import { initializeNavigation } from "./navigation.js";
import { initializeSpeciesPanel } from "./sections/especie.js";
import { initializeCropsPanel } from "./sections/cultivo.js";
import { initializeMeasurementsPanel } from "./sections/medicion.js";
import { initializeHomePanel } from "./sections/home.js";

checkBackendStatus();
initializeNavigation();
initializeSpeciesPanel();
initializeCropsPanel();
initializeMeasurementsPanel();
initializeHomePanel();
