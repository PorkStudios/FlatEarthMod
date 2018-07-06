package it.unibo.direction;

import it.unibo.entity.Route;

import java.io.InputStream;
import java.util.List;

/**
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 *
 */
public interface DirectionParser {

	public List<Route> getRoutes(InputStream is) throws Exception;
}
