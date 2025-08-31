--
-- PostgreSQL database dump
--

-- Dumped from database version 16.3
-- Dumped by pg_dump version 16.3

-- Started on 2024-08-26 23:38:56

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2 (class 3079 OID 16384)
-- Name: adminpack; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS adminpack WITH SCHEMA pg_catalog;


--
-- TOC entry 844 (class 1247 OID 16446)
-- Name: week_day; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.week_day AS ENUM (
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY',
    'SUNDAY'
);


--
-- TOC entry 216 (class 1259 OID 16655)
-- Name: events_current; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.events_current (
                                       date date NOT NULL,
                                       end_time time(6) without time zone NOT NULL,
                                       start_time time(6) without time zone NOT NULL,
                                       id uuid NOT NULL,
                                       user_id uuid,
                                       description text NOT NULL,
                                       template_id uuid
);


--
-- TOC entry 217 (class 1259 OID 16662)
-- Name: events_template; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.events_template (
                                        end_time time(6) without time zone NOT NULL,
                                        start_time time(6) without time zone NOT NULL,
                                        id uuid NOT NULL,
                                        user_id uuid NOT NULL,
                                        week_day public.week_day,
                                        description text NOT NULL,
                                        CONSTRAINT events_template_week_day_check CHECK ((week_day = ANY (ARRAY['MONDAY'::public.week_day, 'TUESDAY'::public.week_day, 'WEDNESDAY'::public.week_day, 'THURSDAY'::public.week_day, 'FRIDAY'::public.week_day, 'SATURDAY'::public.week_day, 'SUNDAY'::public.week_day])))
);


--
-- TOC entry 218 (class 1259 OID 16670)
-- Name: note; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.note (
                             flag boolean DEFAULT false NOT NULL,
                             event_id uuid NOT NULL,
                             id uuid NOT NULL,
                             content text NOT NULL
);


--
-- TOC entry 219 (class 1259 OID 16678)
-- Name: profile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.profile (
                                id uuid NOT NULL,
                                name text NOT NULL
);


--
-- TOC entry 4652 (class 2606 OID 16661)
-- Name: events_current events_current_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.events_current
    ADD CONSTRAINT events_current_pkey PRIMARY KEY (id);


--
-- TOC entry 4654 (class 2606 OID 16669)
-- Name: events_template events_template_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.events_template
    ADD CONSTRAINT events_template_pkey PRIMARY KEY (id);


--
-- TOC entry 4656 (class 2606 OID 16677)
-- Name: note note_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_pkey PRIMARY KEY (id);


--
-- TOC entry 4658 (class 2606 OID 16684)
-- Name: profile profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_pkey PRIMARY KEY (id);


--
-- TOC entry 4662 (class 2606 OID 16695)
-- Name: note fk6010ipgibhm731r4n9loxrb4v; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT fk6010ipgibhm731r4n9loxrb4v FOREIGN KEY (event_id) REFERENCES public.events_current(id);


--
-- TOC entry 4661 (class 2606 OID 16690)
-- Name: events_template fke6wpdvdfpymq9cdq7wrnswotu; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.events_template
    ADD CONSTRAINT fke6wpdvdfpymq9cdq7wrnswotu FOREIGN KEY (user_id) REFERENCES public.profile(id);


--
-- TOC entry 4659 (class 2606 OID 16701)
-- Name: events_current fke7xlvwojiylwnt4shf4kpjjog; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.events_current
    ADD CONSTRAINT fke7xlvwojiylwnt4shf4kpjjog FOREIGN KEY (template_id) REFERENCES public.events_template(id);


--
-- TOC entry 4660 (class 2606 OID 16685)
-- Name: events_current fkmiptb3u8r8ymj332b1k3xhdy5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.events_current
    ADD CONSTRAINT fkmiptb3u8r8ymj332b1k3xhdy5 FOREIGN KEY (user_id) REFERENCES public.profile(id);


-- Completed on 2024-08-26 23:38:56

--
-- PostgreSQL database dump complete
--

