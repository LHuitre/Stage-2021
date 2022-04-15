package requeterrezo;


/*
RequeterRezo
Copyright (C) 2019  Jimmy Benoits

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/


/**
 * Type des noeuds de rezoJDM (http://www.jeuxdemots.org/jdm-about.php).
 *
 * @author jimmy.benoits
 */
public enum TypeNoeud {
    n_generic,
    n_term,
    n_form,
    n_pos,
    n_concept,
    n_flpot,
    n_chunk,
    n_question,
    n_relation,
    r_rule,
    n_analogy, 
    n_commands, 
    f_synt_function,
    n_data,
    n_data_pot,
    n_link,
    n_AKI,
    n_wikipedia,
    non_defini
}
